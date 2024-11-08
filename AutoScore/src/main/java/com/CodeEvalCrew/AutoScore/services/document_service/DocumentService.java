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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamExport;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamQuestion.ExamQuestionExport;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Database;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Question;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamQuestionRepository;
import com.CodeEvalCrew.AutoScore.repositories.examdatabase_repository.IExamDatabaseRepository;
import com.CodeEvalCrew.AutoScore.specification.ExamDatabaseSpecification;

@Service
public class DocumentService implements IDocumentService {

    @Autowired
    private IExamQuestionRepository examQuestionRepository;
    @Autowired
    private IExamPaperRepository examPaperRepository;
    @Autowired
    private IExamDatabaseRepository examDatabaseRepository;

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
        data.put("important", exam.getImportant());
        data.put("semester", exam.getSemester());
        data.put("databaseDescription", exam.getDatabaseDescpription());
        data.put("databaseName", exam.getDatabaseName());
        data.put("databaseNote", exam.getDatabaseNote());

        // Step 3: Load the Word template and replace placeholders
        try (FileInputStream fis = new FileInputStream(templatePath); XWPFDocument document = new XWPFDocument(fis)) {

            // Replace placeholders in paragraphs
            replacePlaceholdersInDocument(document, data);

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

    private ExamExport getExamToExamExport(Long examPaperId) throws NotFoundException, Exception {
        ExamExport result = new ExamExport();
        try {
            List<ExamQuestionExport> questions = new ArrayList<>(getListExamQuestionExport(examPaperId));

            Exam_Paper examPaper = checkEntityExistence(examPaperRepository.findById(examPaperId), "Exam Paper", examPaperId);

            // Exam exam = checkEntityExistence(examRepository.findById(examPaper.getExam().getExamId()), "Exam", examPaper.getExam().getExamId());
            Specification<Exam_Database> spec = ExamDatabaseSpecification.hasForeignKey(examPaperId, "exam_paper", "examPaperId");

            //get error
            Exam_Database examDatabase = examDatabaseRepository.findByExamPaperExamPaperId(examPaperId);

            // result.setExamCode(exam.getExamCode());
            result.setExamPaperCode(examPaper.getExamPaperCode());
            // result.setSemester(exam.getSemester().getSemesterCode());
            // result.setSubjectCode(exam.getSubject().getSubjectCode());
            result.setDuration(90);
            result.setDatabaseDescpription("Database Descpription");
            result.setDatabaseNote("databaseNote");
            result.setQuestions(questions);
            result.setDatabaseName("Database Name");
            result.setDatabaseImage(getImageAsByteArray(""));

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
            ExamQuestionExport export = new ExamQuestionExport();
            // export.setQuestionContent(examQuestion.getQuestionContent());
            // export.setQuestionScore(examQuestion.getMaxScore());
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
            // questionRun.setText("Question: " + question.getQuestionContent() + " (" + question.getQuestionScore() + " points)");

            // Create numbered list for the barems
            // addNumberedBaremList(document, question.getBarems());
            // Add space between questions
            document.createParagraph().createRun();
        }
    }

    public static byte[] getImageAsByteArray(String pathToImage) throws IOException {
        String imgPath = "C:\\Project\\SEP490\\database-img-test.png";
        return Files.readAllBytes(Paths.get(imgPath));
    }

}