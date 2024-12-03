package com.CodeEvalCrew.AutoScore.services.document_service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.mappers.ExamQuestionMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamExport;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamQuestion.ExamQuestionExport;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Exam_Status_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Database;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Question;
import com.CodeEvalCrew.AutoScore.models.Entity.Important;
import com.CodeEvalCrew.AutoScore.models.Entity.Important_Exam_Paper;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamQuestionRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamRepository;
import com.CodeEvalCrew.AutoScore.repositories.examdatabase_repository.IExamDatabaseRepository;
import com.CodeEvalCrew.AutoScore.repositories.important_repository.ImportantExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.important_repository.ImportantRepository;
import com.CodeEvalCrew.AutoScore.utils.Util;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Service
public class DocumentService implements IDocumentService {

    @Autowired
    private IExamQuestionRepository examQuestionRepository;
    @Autowired
    private IExamPaperRepository examPaperRepository;
    @Autowired
    private IExamDatabaseRepository examDatabaseRepository;
    @Autowired
    private IExamRepository examRepository;
    @Autowired
    private ImportantRepository importantRepository;
    @Autowired
    private ImportantExamPaperRepository importantExamPaperRepository;

    @Override
    public byte[] mergeDataToWord(Long examPaperId) throws Exception, NotFoundException {
        ExamExport exam = new ExamExport();
        // Step 1: Get the Exam data
        try {
            exam = getExamToExamExport(examPaperId);
        } catch (NotFoundException ex) {
            throw new NotFoundException(ex.getMessage());
        }

        // Define the path of the template and output file
        String templatePath = "AutoScore\\src\\main\\resources\\Template.docx";
        String outputPath = "C:\\Project\\SEP490\\output.docx";

        Map<String, String> data = new HashMap<>();

        // Step 2: Prepare the placeholder data
        String duration = Integer.toString(exam.getDuration());
        data.put("examCode", exam.getExamCode());
        data.put("examPaperCode", exam.getExamPaperCode());
        data.put("subjectCode", exam.getSubjectCode());
        data.put("duration", duration);
        // data.put("instructions", exam.getInstructions());
        data.put("semester", exam.getSemester());
        data.put("databaseDescription", exam.getDatabaseDescpription());
        data.put("databaseName", exam.getDatabaseName());
        data.put("databaseNote", exam.getDatabaseNote());

        // Step 3: Load the Word template and replace placeholders
        try (FileInputStream fis = new FileInputStream(templatePath); XWPFDocument document = new XWPFDocument(fis)) {
            addInstruction(document, exam.getInstructions());
            // Replace placeholders in paragraphs
            replacePlaceholdersInDocument(document, data);
            //add important to the word
            fillImportantField(document, exam);
            // Add image in to the exam paper
            addDatabaseImage(document, exam);
            // Add exam questions and their corresponding barems
            addExamQuestions(document, exam);
            // Step 4: Save the updated document
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                document.write(fos);
                fos.close();
            }
        }
        File file = new File(outputPath);
        byte[] documentContent;
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            documentContent = fileInputStream.readAllBytes();
        }
        file.delete();
        return documentContent;
    }

    private void fillImportantField(XWPFDocument document, ExamExport exam) {
        List<Long> importantIds = exam.getImportants();

        if (importantIds == null || importantIds.isEmpty()) {
            return;  // Exit if there are no important IDs to process
        }

        // Locate {important} placeholder
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            for (XWPFRun run : paragraph.getRuns()) {
                String text = run.getText(0);
                if (text != null && text.contains("{important}")) {
                    // Clear existing text and add the title for important instructions
                    run.setText("IMPORTANT – before you start doing your solution, MUST do the following steps:", 0);
                    run.addBreak();

                    // Iterate over each important ID and add the corresponding instruction with breaks
                    for (Long importantId : importantIds) {
                        String importantText = getImportantText(importantId);
                        if (importantText != null) {
                            run.setText("\t" + importantText);
                            run.addBreak();  // Additional line break after each step for spacing
                        }
                    }
                    break;  // Stop after replacing {important}
                }
            }
        }
    }

    private String getImportantText(Long importantId) {
        // Logic to retrieve the information for the important ID
        Optional<Important> important = importantRepository.findById(importantId);
        if (important.isEmpty()) {
            return "";
        }
        return important.get().getImportantScrip();
        // Return null if no matching ID is found
    }

    private ExamExport getExamToExamExport(Long examPaperId) throws NotFoundException, Exception {
        ExamExport result = new ExamExport();
        try {
            //getListQeustion
            List<ExamQuestionExport> questions = new ArrayList<>(getListExamQuestionExport(examPaperId));

            //getExam paper
            Exam_Paper examPaper = checkEntityExistence(examPaperRepository.findById(examPaperId), "Exam Paper", examPaperId);

            //check exam
            Exam exam = checkEntityExistence(examRepository.findById(examPaper.getExam().getExamId()), "Exam", examPaper.getExam().getExamId());

            //get list important id
            List<Important_Exam_Paper> importants = importantExamPaperRepository.findImportantExamPaperIdByExamPaper_ExamPaperId(examPaperId);
            List<Long> importantIds = new ArrayList<>();
            for (Important_Exam_Paper important : importants) {
                importantIds.add(important.getImportant().getImportantId());
            }

            //get exam database
            Exam_Database examDatabase = examDatabaseRepository.findByExamPaperExamPaperId(examPaperId);

            result.setExamCode(exam.getExamCode());
            result.setExamPaperCode(examPaper.getExamPaperCode());
            result.setSemester(exam.getSemester().getSemesterCode());
            result.setSubjectCode(exam.getSubject().getSubjectCode());
            result.setInstructions(examPaper.getInstruction());
            result.setImportants(importantIds);
            result.setDuration(examPaper.getDuration());
            result.setQuestions(questions);
            result.setDatabaseDescpription(examDatabase.getDatabaseDescription());
            result.setDatabaseNote(examDatabase.getDatabaseNote());
            result.setDatabaseName(examDatabase.getDatabaseName());
            result.setDatabaseImage(examDatabase.getDatabaseImage());

        } catch (NotFoundException ex) {
            throw ex;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }

        return result;
    }

    private List<ExamQuestionExport> getListExamQuestionExport(Long examPaperId) throws NoSuchElementException {
        List<ExamQuestionExport> result = new ArrayList<>();

        List<Exam_Question> listQuestions = examQuestionRepository.getByExamPaperExamPaperId(examPaperId);

        if (listQuestions.isEmpty()) {
            throw new NoSuchElementException("No question found");
        }

        for (Exam_Question examQuestion : listQuestions) {
            ExamQuestionExport export = ExamQuestionMapper.INSTANCE.questionToExport(examQuestion);
            result.add(export);
        }

        return result;
    }

    private <T> T checkEntityExistence(Optional<T> entity, String entityName, Long entityId) throws NotFoundException {
        return entity.orElseThrow(() -> new NotFoundException(entityName + " id: " + entityId + " not found"));
    }

    private void replacePlaceholdersInDocument(XWPFDocument document, Map<String, String> data) {
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            // combine run replace
            StringBuilder paragraphText = new StringBuilder();
            for (XWPFRun run : paragraph.getRuns()) {
                paragraphText.append(run.getText(0));
            }

            String combinedText = paragraphText.toString();
            for (Map.Entry<String, String> entry : data.entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                if (combinedText.contains(placeholder)) {
                    combinedText = combinedText.replace(placeholder, entry.getValue());
                }
            }

            // update run
            int runIndex = 0;
            for (XWPFRun run : paragraph.getRuns()) {
                if (runIndex == 0) {
                    run.setText(combinedText, 0); // update run
                } else {
                    run.setText("", 0); // remove value of run
                }
                runIndex++;
            }
        }
    }

    private void addInstruction(XWPFDocument document, String value) {
// Locate {important} placeholder
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            for (XWPFRun run : paragraph.getRuns()) {
                String text = run.getText(0);
                if (text != null && text.contains("{instructions}")) {
                    XWPFParagraph valueParagraph = document.createParagraph();
                    valueParagraph.setIndentationLeft(720); // Thụt vào 720 (đơn vị là twips, tương đương 0.5 inch)
                    XWPFRun valueRun = valueParagraph.createRun();
                    // Thêm giá trị với cách xuống dòng nếu có nhiều dòng
                    String[] lines = value.split("\n");
                    for (int i = 0; i < lines.length; i++) {
                        valueRun.setText(lines[i]);
                        if (i < lines.length - 1) {
                            valueRun.addBreak();
                        }
                    }
                }
            }
        }
    }

    private String addDatabaseImage(XWPFDocument document, ExamExport exportExam) throws IOException {
        String message = "Image added successfully.";
        String placeholder = "${imagePlaceholder}";

        try {
            List<XWPFParagraph> paragraphs = document.getParagraphs();

            for (XWPFParagraph paragraph : paragraphs) {
                for (XWPFRun run : paragraph.getRuns()) {
                    String text = run.getText(0);
                    if (text != null && text.contains(placeholder)) {
                        // Replace the placeholder text with an empty string
                        text = text.replace(placeholder, "");
                        run.setText(text, 0); // Update the run with the modified text

                        // Create a new centered paragraph for the image
                        XWPFParagraph imageParagraph = document.createParagraph();
                        imageParagraph.setAlignment(ParagraphAlignment.CENTER); // Center align the paragraph
                        XWPFRun imageRun = imageParagraph.createRun();

                        try (ByteArrayInputStream imageInputStream = new ByteArrayInputStream(exportExam.getDatabaseImage())) {
                            imageRun.addPicture(
                                    imageInputStream,
                                    XWPFDocument.PICTURE_TYPE_PNG, // Correct image type
                                    "DatabaseImage.png", // Image name (for internal Word reference)
                                    Units.toEMU(300), // Width in EMUs
                                    Units.toEMU(200) // Height in EMUs
                            );
                        } catch (InvalidFormatException e) {
                            message = "Error adding image to document.";
                            e.printStackTrace();
                        }

                        // Optional: Add a break after the image
                        imageRun.addBreak(BreakType.PAGE);
                        break; // Exit the loop after processing the first run with the placeholder
                    }
                }
            }
        } catch (IOException e) {
            message = "Error reading the image file.";
        } catch (Exception e) {
            message = "An unexpected error occurred.";
        }

        return message;
    }

    private void addExamQuestions(XWPFDocument document, ExamExport exam) {
        for (ExamQuestionExport question : exam.getQuestions()) {
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun boldRun = paragraph.createRun();
            XWPFRun run = paragraph.createRun();

            boldRun.setBold(true);
            boldRun.setText(question.getQuestionContent() + " (" + question.getExamQuestionScore().toString() + " Points)");
            boldRun.addBreak();

            run.setText("End Point: " + question.getHttpMethod() + " " + question.getEndPoint());
            run.addBreak();

            run.setText("Role Allow: " + question.getRoleAllow());
            run.addBreak();

            run.setText("Description: " + question.getDescription());
            run.addBreak();

            if (question.getPayloadType() != null) {
                run.setText("Request type: " + question.getPayloadType());
            }
            if (question.getPayload() != null) {
                addFieldToDocument(document, "Request:", question.getPayload());
            }
            if (question.getValidation() != null) {
                addFieldToDocument(document, "Validation: ", question.getValidation());
            }
            if (question.getSucessResponse() != null) {
                addFieldToDocument(document, "Success Response: ", question.getSucessResponse());
            }
            if (question.getErrorResponse() != null) {
                addFieldToDocument(document, "Error Response: ", question.getErrorResponse());
            }

            // Add a separator between items
            XWPFParagraph separator = document.createParagraph();
            XWPFRun separatorRun = separator.createRun();
            separatorRun.addBreak();
        }
    }

    public static void writeDynamicJsonToWord(XWPFDocument document, String jsonString, String title) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Chuyển chuỗi JSON thành đối tượng tổng quát (Map hoặc List)
            Object jsonData = objectMapper.readValue(jsonString, new TypeReference<>() {
            });

            // Ghi nội dung JSON vào tài liệu Word
            // writeJsonObject(document, jsonData, 0);
            writeJsonToWord(document, jsonString);

        } catch (IOException e) {
            addFieldToDocument(document, title, jsonString);
        }
    }

    // Hàm ghi nội dung JSON vào tài liệu Word
    // private static void writeJsonObject(XWPFDocument document, Object jsonObject, int indentLevel) {
    //     switch (jsonObject) {
    //         case Map<?, ?> map -> {
    //             for (Map.Entry<?, ?> entry : map.entrySet()) {
    //                 // Ghi key
    //                 addIndentedText(document, entry.getKey() + ":", indentLevel, true); // In đậm cho key
    //                 // Đệ quy ghi value
    //                 writeJsonObject(document, entry.getValue(), indentLevel + 1);
    //             }
    //         }
    //         case List<?> list -> {
    //             for (Object item : list) {
    //                 // Ghi từng phần tử của List với dấu gạch đầu dòng
    //                 addIndentedText(document, "- ", indentLevel, false);
    //                 writeJsonObject(document, item, indentLevel + 1);
    //             }
    //         }
    //         default -> // Ghi giá trị đơn giản (String, Number, Boolean, v.v.)
    //             addIndentedText(document, jsonObject.toString(), indentLevel, false);
    //     }
    // }
    public static void writeJsonToWord(XWPFDocument document, String jsonString) {
        try {
            // Chuyển chuỗi JSON thành dạng pretty JSON
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Bật thụt lề
            Object jsonObject = objectMapper.readValue(jsonString, Object.class); // Đọc JSON
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject); // Tạo chuỗi JSON định dạng đẹp

            // Ghi JSON vào tài liệu
            addJsonToDocument(document, prettyJson);

        } catch (IOException e) {
            // Xử lý lỗi nếu chuỗi JSON không hợp lệ
            addJsonToDocument(document, jsonString);
        }
    }

    // Ghi chuỗi JSON vào tài liệu Word
    private static void addJsonToDocument(XWPFDocument document, String json) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setFontFamily("Courier New"); // Sử dụng font monospace để giữ định dạng JSON
        run.setFontSize(12); // Kích thước chữ
        run.setText(json);
    }

    // Ghi chuỗi JSON vào tài liệu Word
    // private static void addJsonStringToDocument(XWPFDocument document, String jsonString) {
    //     XWPFParagraph paragraph = document.createParagraph();
    //     XWPFRun run = paragraph.createRun();
    //     run.setFontFamily("Courier New"); // Dùng font monospace để giữ nguyên định dạng JSON
    //     run.setFontSize(12); // Kích thước chữ
    //     run.setText(jsonString);
    // }
// Hàm hỗ trợ ghi văn bản với mức thụt lề và định dạng
    // private static void addIndentedText(XWPFDocument document, String text, int indentLevel, boolean isBold) {
    //     XWPFParagraph paragraph = document.createParagraph();
    //     XWPFRun run = paragraph.createRun();
    //     run.setText(text);
    //     run.setBold(isBold);
    //     run.setFontFamily("Courier New"); // Dùng font monospace cho dữ liệu JSON
    //     run.setFontSize(12); // Kích thước chữ cố định
    //     paragraph.setIndentationLeft(indentLevel * 300); // 300 = 0.3 inch mỗi cấp
    // }
    public static byte[] getImageAsByteArray(String pathToImage) throws IOException {
        String imgPath = "C:\\Project\\SEP490\\database-img-test.png";
        return Files.readAllBytes(Paths.get(imgPath));
    }

    private static void addFieldToDocument(XWPFDocument document, String fieldName, String fieldValue) {
        if (fieldValue.isBlank() || fieldValue.isBlank()) {
            return;
        }

        // Tạo một đoạn mới cho tên trường
        XWPFParagraph fieldParagraph = document.createParagraph();
        XWPFRun fieldRun = fieldParagraph.createRun();
        // Định dạng tên trường
        fieldRun.setBold(true);
        fieldRun.setText(fieldName);
        // Tạo một đoạn mới cho giá trị
        XWPFParagraph valueParagraph = document.createParagraph();
        valueParagraph.setIndentationLeft(720); // Thụt vào 720 (đơn vị là twips, tương đương 0.5 inch)
        XWPFRun valueRun = valueParagraph.createRun();
        // Thêm giá trị với cách xuống dòng nếu có nhiều dòng
        String[] lines = fieldValue.split("\n");
        for (int i = 0; i < lines.length; i++) {
            valueRun.setText(lines[i]);
            if (i < lines.length - 1) {
                valueRun.addBreak();
            }
        }

    }

    @Override
    public void importExamPaper(Long examPaperId, MultipartFile multipartFile) throws Exception, NotFoundException {
        try {
            //check exam Paper
            Exam_Paper examPaper = checkEntityExistence(examPaperRepository.findById(examPaperId), "ExamPaper", examPaperId);

            Long createBy = Util.getAuthenticatedAccountId();

            File file = convertToFile(multipartFile);
            Long index = 1l;
            List<String> questions = extractQuestions2(file);
            for (String question : questions) {
                Exam_Question examQuestion = parseExamQuestion(question);
                examQuestion.setExamPaper(examPaper);
                examQuestion.setStatus(Exam_Status_Enum.ACTIVE);
                examQuestion.setOrderBy(index);
                examQuestion.setCreatedAt(Util.getCurrentDateTime());
                examQuestion.setCreatedBy(createBy);
                index++;

                examQuestionRepository.save(examQuestion);

            }

        } catch (IOException | NotFoundException e) {
            throw e;
        }
    }

    public static File convertToFile(MultipartFile multipartFile) throws IOException {
        // Tạo một file tạm
        File tempFile = File.createTempFile("upload-", multipartFile.getOriginalFilename());
        // Chuyển dữ liệu từ MultipartFile vào file tạm
        multipartFile.transferTo(tempFile);
        // Trả về file
        return tempFile;
    }

    // private static List<Exam_Question> extractQuestions(File wordFile) throws IOException {
    //     List<Exam_Question> questions = new ArrayList<>();
    //     try (FileInputStream fis = new FileInputStream(wordFile); XWPFDocument document = new XWPFDocument(fis)) {
    //         List<XWPFParagraph> paragraphs = document.getParagraphs();
    //         Exam_Question currentQuestion = null;
    //         for (XWPFParagraph paragraph : paragraphs) {
    //             String text = paragraph.getText().trim();
    //             if (text.isEmpty()) {
    //                 continue; // Bỏ qua đoạn trống
    //             }
    //             // Bắt đầu một câu hỏi mới
    //             if (text.startsWith("Endpoint:")) {
    //                 if (currentQuestion != null) {
    //                     questions.add(currentQuestion); // Lưu câu hỏi trước đó
    //                 }
    //                 currentQuestion = new Exam_Question();
    //                 currentQuestion.setEndPoint(text.substring(9).trim());
    //                 continue;
    //             }
    //             // Ánh xạ các trường thông tin
    //             if (text.startsWith("HTTP Method:")) {
    //                 if (currentQuestion != null) {
    //                     currentQuestion.setHttpMethod(text.substring(13).trim());
    //                 }
    //             } else if (text.startsWith("Role Allowed:")) {
    //                 if (currentQuestion != null) {
    //                     currentQuestion.setRoleAllow(text.substring(13).trim());
    //                 }
    //             } else if (text.startsWith("Function:")) {
    //                 if (currentQuestion != null) {
    //                     currentQuestion.setDescription(text.substring(9).trim());
    //                 }
    //             } else if (text.startsWith("Validations:")) {
    //                 if (currentQuestion != null) {
    //                     currentQuestion.setValidation(text.substring(12).trim());
    //                 }
    //             } else if (text.startsWith("Success Response:")) {
    //                 if (currentQuestion != null) {
    //                     currentQuestion.setSucessResponse(text.substring(17).trim());
    //                 }
    //             } else if (text.startsWith("Error Response:")) {
    //                 if (currentQuestion != null) {
    //                     currentQuestion.setErrorResponse(text.substring(15).trim());
    //                 }
    //             } else if (text.startsWith("Request Body:")) {
    //                 if (currentQuestion != null) {
    //                     currentQuestion.setPayload(text.substring(13).trim());
    //                 }
    //             }
    //             // Nếu đoạn văn không bắt đầu bằng trường nào, kiểm tra nội dung câu hỏi
    //             if (currentQuestion != null && currentQuestion.getQuestionContent() == null) {
    //                 currentQuestion.setQuestionContent(text); // Lưu nội dung câu hỏi
    //             }
    //         }
    //         // Lưu câu hỏi cuối cùng (nếu có)
    //         if (currentQuestion != null) {
    //             questions.add(currentQuestion);
    //         }
    //     }
    //     return questions;
    // }
    // public static void main(String[] args) {
    //     try {
    //         String file = "C:\\Project\\SEP490\\autoscore-backend\\AutoScore\\src\\main\\resources\\AutoScoreExamPapperTemplate.docx";
    //         // Đường dẫn tới file Word
    //         File wordFile = new File("C:\\Project\\SEP490\\autoscore-backend\\AutoScore\\src\\main\\resources\\AutoScoreExamPapperTemplate.docx");
    //         List<String> questions = extractQuestions2(wordFile);
    //         System.out.println("Questions:");
    //         for (String question : questions) {
    //             // System.out.println(question);
    //             System.out.println("====================================");
    //             Exam_Question examQuestion = parseExamQuestion(question);
    //             System.out.println("Question Content: " + examQuestion.getQuestionContent());
    //             System.out.println("Endpoint: " + examQuestion.getEndPoint());
    //             System.out.println("HTTP Method: " + examQuestion.getHttpMethod());
    //             System.out.println("Role Allowed: " + examQuestion.getRoleAllow());
    //             System.out.println("Description: " + examQuestion.getDescription());
    //             System.out.println("Request Body: " + examQuestion.getPayload());
    //             System.out.println("Success Response: " + examQuestion.getSucessResponse());
    //             System.out.println("Error Response: " + examQuestion.getErrorResponse());
    //             System.out.println("Question Score: " + examQuestion.getExamQuestionScore());
    //         }
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }
    public static List<String> extractQuestions(String filePath) {
        List<String> questions = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(new File(filePath)); XWPFDocument document = new XWPFDocument(fis)) {

            // Duyệt qua từng đoạn văn bản
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText().trim();

                // Kiểm tra nếu đoạn bắt đầu bằng "Question"
                if (text.startsWith("Question ")) {
                    questions.add(text);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return questions;
    }

    public static Map<String, String> extractQuestionsWithContent(String filePath) {
        Map<String, String> questionsWithContent = new LinkedHashMap<>(); // Đảm bảo thứ tự
        try (FileInputStream fis = new FileInputStream(new File(filePath)); XWPFDocument document = new XWPFDocument(fis)) {

            List<XWPFParagraph> paragraphs = document.getParagraphs();
            String currentQuestion = null;
            StringBuilder currentContent = new StringBuilder();

            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText().trim();

                // Nếu đoạn bắt đầu bằng "Question", lưu câu hỏi hiện tại
                if (text.startsWith("Question ")) {
                    // Lưu nội dung của câu hỏi trước đó (nếu có)
                    if (currentQuestion != null) {
                        questionsWithContent.put(currentQuestion, currentContent.toString().trim());
                    }
                    currentQuestion = text;
                    currentContent.setLength(0); // Xóa nội dung cũ
                } else if (currentQuestion != null) {
                    // Thêm đoạn văn bản vào nội dung hiện tại
                    currentContent.append(text).append("\n");
                }
            }

            // Lưu nội dung của câu hỏi cuối cùng
            if (currentQuestion != null) {
                questionsWithContent.put(currentQuestion, currentContent.toString().trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return questionsWithContent;
    }

    public static Map<String, String> parseQuestion(String question) {
        Map<String, String> fields = new HashMap<>();
        String[] lines = question.split("\n");

        String currentField = null;
        StringBuilder fieldContent = new StringBuilder();

        for (String line : lines) {
            line = line.trim();

            // Kiểm tra xem dòng có phải là một tiêu đề trường không
            if (line.matches("^(Question|Endpoint|HTTP Method|Role Allowed|Function|Request Body|Validations|Success Response|Error Response):.*")) {
                // Lưu nội dung của trường trước đó (nếu có)
                if (currentField != null) {
                    fields.put(currentField, fieldContent.toString().trim());
                }

                // Bắt đầu một trường mới
                int colonIndex = line.indexOf(":");
                currentField = line.substring(0, colonIndex);
                fieldContent = new StringBuilder(line.substring(colonIndex + 1).trim());
            } else if (currentField != null) {
                // Tiếp tục nối nội dung vào trường hiện tại
                fieldContent.append(" ").append(line);
            }
        }

        // Lưu nội dung của trường cuối cùng
        if (currentField != null) {
            fields.put(currentField, fieldContent.toString().trim());
        }

        return fields;
    }

    public static Exam_Question mapToEntity(Map<String, String> fields) {
        Exam_Question entity = new Exam_Question();

        entity.setQuestionContent(fields.getOrDefault("Question", ""));
        entity.setEndPoint(fields.getOrDefault("Endpoint", ""));
        entity.setHttpMethod(fields.getOrDefault("HTTP Method", ""));
        entity.setRoleAllow(fields.getOrDefault("Role Allowed", ""));
        entity.setDescription(fields.getOrDefault("Function", ""));
        entity.setPayload(fields.getOrDefault("Request Body", ""));
        entity.setValidation(fields.getOrDefault("Validations", ""));
        entity.setSucessResponse(fields.getOrDefault("Success Response", ""));
        entity.setErrorResponse(fields.getOrDefault("Error Response", ""));

        return entity;
    }

    public static Exam_Question parseQuestionToEntity(String question) {
        Exam_Question entity = new Exam_Question();
        String[] lines = question.split("\n");

        String currentField = null;
        StringBuilder fieldContent = new StringBuilder();

        for (String line : lines) {
            line = line.trim();

            // Xử lý tiêu đề câu hỏi
            if (line.startsWith("Question ")) {
                // Lấy tên câu hỏi
                int scoreIndex = line.indexOf("(");
                if (scoreIndex != -1) {
                    String name = line.substring(0, scoreIndex).trim();
                    String scoreStr = line.substring(scoreIndex + 1, line.indexOf(")")).replace("Score", "").trim();
                    entity.setQuestionContent(name);
                    entity.setExamQuestionScore(Float.valueOf(scoreStr));
                } else {
                    entity.setQuestionContent(line);
                }
            } else if (line.matches("^(Content|Endpoint|HTTP Method|Role Allowed|Function|Request Body|Validations|Response):.*")) {
                // Lưu nội dung trường trước đó
                if (currentField != null) {
                    setFieldToEntity(entity, currentField, fieldContent.toString().trim());
                }

                // Xác định trường mới
                int colonIndex = line.indexOf(":");
                currentField = line.substring(0, colonIndex);
                fieldContent = new StringBuilder(line.substring(colonIndex + 1).trim());
            } else if (currentField != null) {
                // Nối nội dung vào trường hiện tại
                fieldContent.append(" ").append(line);
            }
        }

        // Lưu trường cuối cùng
        if (currentField != null) {
            setFieldToEntity(entity, currentField, fieldContent.toString().trim());
        }

        return entity;
    }

    private static void setFieldToEntity(Exam_Question entity, String field, String value) {
        switch (field) {
            case "Content" ->
                entity.setQuestionContent(value);
            case "Endpoint" ->
                entity.setEndPoint(value);
            case "HTTP Method" ->
                entity.setHttpMethod(value);
            case "Role Allowed" ->
                entity.setRoleAllow(value);
            case "Function" ->
                entity.setDescription(value);
            case "Request Body" ->
                entity.setPayload(value);
            case "Validations" ->
                entity.setValidation(value);
            case "Success Response" ->
                entity.setSucessResponse(value);
            case "Error Response" ->
                entity.setErrorResponse(value);
        }
    }

    public static Exam_Question parseExamQuestion(String input) {
        // Khởi tạo đối tượng Exam_Question mới
        Exam_Question examQuestion = new Exam_Question();

        // Cấu trúc Regex để tìm các phần trong câu hỏi
        String questionContentPattern = "Question: (.*?)\\n"; // Tìm nội dung câu hỏi
        String endPointPattern = "Endpoint: (.*?)\\n"; // Tìm endpoint
        String httpMethodPattern = "HTTP Method: (.*?)\\n"; // Tìm phương thức HTTP
        String roleAllowPattern = "Role Allowed: (.*?)\\n"; // Tìm vai trò được phép
        String descriptionPattern = "Function: (.*?)\\n"; // Tìm mô tả chức năng
        String payloadTypePattern = "Request Type: \\s*:\\s*(.*?)\\n"; // Tìm loại payload (Payload Type)
        String requestBodyPattern = "Request Body\\s*\\{(.*?)\\}"; // Tìm request body
        String validationsPattern = "Validations:\\s*(.*?)\\n(\\s*•.*?\\n)*"; // Tìm validation
        String successResponsePattern = "Success Response:(.*?)Error Response:"; // Tìm phản hồi thành công
        String errorResponsePattern = "Error Response:(.*?)$"; // Tìm phản hồi lỗi
        String questionScorePattern = "Score: (\\d+\\.\\d+)"; // Tìm điểm câu hỏi (ví dụ: Score: 2.0)

        // Sử dụng Pattern và Matcher để tìm kiếm các phần trong chuỗi
        examQuestion.setQuestionContent(getPatternMatch(input, questionContentPattern));
        examQuestion.setEndPoint(getPatternMatch(input, endPointPattern));
        examQuestion.setHttpMethod(getPatternMatch(input, httpMethodPattern));
        examQuestion.setRoleAllow(getPatternMatch(input, roleAllowPattern));
        examQuestion.setDescription(getPatternMatch(input, descriptionPattern));
        examQuestion.setPayloadType(getPatternMatch(input, payloadTypePattern)); // Gán Payload Type
        examQuestion.setPayload(getPatternMatch(input, requestBodyPattern));
        examQuestion.setValidation(getPatternMatch(input, validationsPattern)); // Gán Validation
        examQuestion.setSucessResponse(getPatternMatch(input, successResponsePattern));
        examQuestion.setErrorResponse(getPatternMatch(input, errorResponsePattern));

        // Lấy điểm câu hỏi từ cuối cùng của nội dung câu hỏi
        String scoreString = getPatternMatch(input, questionScorePattern);
        if (!scoreString.isEmpty()) {
            examQuestion.setExamQuestionScore(Float.valueOf(scoreString)); // Gán điểm cho câu hỏi
        } else {
            examQuestion.setExamQuestionScore(2.0f); // Điểm mặc định nếu không tìm thấy
        }

        return examQuestion;
    }

    // Hàm phụ trợ để tìm kiếm và lấy giá trị của mẫu regex
    private static String getPatternMatch(String input, String pattern) {
        Pattern p = Pattern.compile(pattern, Pattern.DOTALL); // Sử dụng DOTALL để bao quát cả dòng mới
        Matcher m = p.matcher(input);
        if (m.find()) {
            return m.group(1).trim(); // Trả về phần được tìm thấy trong nhóm đầu tiên
        }
        return ""; // Trả về chuỗi rỗng nếu không tìm thấy
    }

    public static List<String> extractQuestions2(File file) {
        List<String> questions = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file); XWPFDocument document = new XWPFDocument(fis)) {

            List<XWPFParagraph> paragraphs = document.getParagraphs();
            StringBuilder currentQuestion = new StringBuilder();
            boolean isQuestionSection = false;

            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText().trim();

                // Bắt đầu câu hỏi mới
                if (text.startsWith("Question:")) {
                    // Nếu có câu hỏi hiện tại đang được thu thập, lưu lại
                    if (currentQuestion.length() > 0) {
                        questions.add(currentQuestion.toString().trim());
                        currentQuestion.setLength(0); // Reset buffer
                    }
                    isQuestionSection = true; // Bắt đầu thu thập câu hỏi
                }

                // Nếu đang thu thập câu hỏi, tiếp tục thêm nội dung
                if (isQuestionSection) {
                    if (!text.isEmpty()) { // Bỏ qua dòng trống
                        currentQuestion.append(text).append("\n");
                    }
                }
            }

            // Thêm câu hỏi cuối cùng nếu có
            if (currentQuestion.length() > 0) {
                questions.add(currentQuestion.toString().trim());
            }

        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }

        return questions;
    }

}
