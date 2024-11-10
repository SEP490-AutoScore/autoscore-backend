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
            addImportant(document, exam);

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
        byte[] documentContent = new FileInputStream(file).readAllBytes();
        return documentContent;
    }

    private void addImportant(XWPFDocument document, ExamExport exam) {
        // Assuming 'important' is a list of important IDs
        List<Long> importantIds = exam.getImportants();

        // Check if the list is not empty
        if (importantIds != null && !importantIds.isEmpty()) {
            // Create a new paragraph to add the important information
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText("IMPORTANT – Before you begin, you MUST complete the following steps:\n");

            // Iterate through the list of important IDs and add corresponding information
            for (Long importantId : importantIds) {
                String importantText = getImportantText(importantId); // Retrieve the text for the ID
                if (importantText != null) {
                    run.addBreak(); // Add a line break between steps
                    run.setText(importantText);
                }

            }
        }
    }


    //fortest
    private String getImportantText(Long importantId) {
        // Logic to retrieve the information for the important ID
        // This can be a database lookup or a static mapping
        switch (importantId) {
            case 1L: return "Step 1: Read the instructions carefully.";
            case 2L: return "Step 2: Ensure your environment is set up correctly.";
            case 3L: return "Step 3: Review the exam topics.";
            // Add more cases as needed
            default: return " thissadsfnhsabdfunull"; // Return null if no matching ID is found
        }
    }

    private ExamExport getExamToExamExport(Long examPaperId) throws NotFoundException, Exception {
        ExamExport result = new ExamExport();
        try {
            List<ExamQuestionExport> questions = new ArrayList<>(getListExamQuestionExport(examPaperId));

            //getExam paper
            Exam_Paper examPaper = checkEntityExistence(examPaperRepository.findById(examPaperId), "Exam Paper", examPaperId);

            //check exam
            Exam exam = checkEntityExistence(examRepository.findById(examPaper.getExam().getExamId()), "Exam", examPaper.getExam().getExamId());

            //get list important id
            List<Long> importantIds = importantExamPaperRepository.findImportantExamPaperIdByExamPaper_ExamPaperId(examPaperId);

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
            result.setDatabaseDescpription("Database Descpription");
            // result.setDatabaseNote(examDatabase.getDatabaseNote());
            // result.setDatabaseName(examDatabase.getDatabaseName());
            // result.setDatabaseImage(examDatabase.getDatabaseImage());
            result.setDatabaseNote("examDatabase.getDatabaseNote()");
            result.setDatabaseName("examDatabase.getDatabaseName()");
            result.setDatabaseImage(getImageAsByteArray("pathToImage"));

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
                                    "image.png", // Image name (for internal Word reference)
                                    Units.toEMU(200), // Width in EMUs
                                    Units.toEMU(150) // Height in EMUs
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
            e.printStackTrace();
        } catch (Exception e) {
            message = "An unexpected error occurred.";
            e.printStackTrace();
        }

        return message;
    }

    private void addExamQuestions(XWPFDocument document, ExamExport exam) {
        for (ExamQuestionExport question : exam.getQuestions()) {
            // Create a paragraph for the question content and score
            XWPFParagraph questionParagraph = document.createParagraph();
            XWPFRun questionRun = questionParagraph.createRun();
            questionRun.setBold(true);  // Highlight the question
            document.createParagraph().createRun();
        }
    }

    public static byte[] getImageAsByteArray(String pathToImage) throws IOException {
        String imgPath = "C:\\Project\\SEP490\\database-img-test.png";
        return Files.readAllBytes(Paths.get(imgPath));
    }

}
