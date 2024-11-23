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
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.mappers.ExamQuestionMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamExport;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamQuestion.ExamQuestionExport;
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
        data.put("instructions", exam.getInstructions());
        data.put("semester", exam.getSemester());
        data.put("databaseDescription", exam.getDatabaseDescpription());
        data.put("databaseName", exam.getDatabaseName());
        data.put("databaseNote", exam.getDatabaseNote());

        // Step 3: Load the Word template and replace placeholders
        try (FileInputStream fis = new FileInputStream(templatePath); XWPFDocument document = new XWPFDocument(fis)) {

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

    //fortest
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

            addFieldToDocument(document, "Request body (" + question.getPayloadType() + ")", question.getPayload());
            addFieldToDocument(document, "Validation: ", question.getValidation());
            addFieldToDocument(document, "Success Response: ", question.getSucessResponse());
            addFieldToDocument(document, "Error Response: ", question.getErrorResponse());

            // Add a separator between items
            XWPFParagraph separator = document.createParagraph();
            XWPFRun separatorRun = separator.createRun();
            separatorRun.addBreak();
        }
    }

    public static byte[] getImageAsByteArray(String pathToImage) throws IOException {
        String imgPath = "C:\\Project\\SEP490\\database-img-test.png";
        return Files.readAllBytes(Paths.get(imgPath));
    }

    private static void addFieldToDocument(XWPFDocument document, String fieldName, String fieldValue) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();

        XWPFRun valueRun = paragraph.createRun();

        if (fieldValue != null) {
            run.setBold(true);
            run.setText(fieldName);
            run.addBreak();
            String[] lines = fieldValue.split("\n");
            for (int i = 0; i < lines.length; i++) {
                valueRun.setText(lines[i]);
                if (i < lines.length - 1) { // Add a break after each line except the last one
                    valueRun.addBreak();
                }
            }
        } else {

        }
    }
}
