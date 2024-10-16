package com.CodeEvalCrew.AutoScore.services.exam_service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFAbstractNum;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFNumbering;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.model.fields.merge.MailMerger;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTAbstractNum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.mappers.ExamMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamCreateRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamExport;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamViewRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamBarem.ExamBaremExport;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamQuestion.ExamQuestionExport;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Testcase.TestCaseExport;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamViewResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam;
import com.CodeEvalCrew.AutoScore.models.Entity.Subject;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IEmployeeRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamRepository;
import com.CodeEvalCrew.AutoScore.repositories.subject_repository.ISubjectRepository;
import com.CodeEvalCrew.AutoScore.specification.ExamSpecification;
import com.CodeEvalCrew.AutoScore.utils.Util;
import com.aspose.words.Document;
import com.aspose.words.MailMerge;

import jakarta.transaction.Transactional;

@Service
public class ExamService implements IExamService {

    @Autowired
    private final IExamRepository examRepository;

    @Autowired
    private final ISubjectRepository subjectRepository;

    @Autowired
    private final IAccountRepository accountRepository;
    private final Util util;

    public ExamService(IExamRepository examRepository,
            ISubjectRepository subjectRepository,
            IAccountRepository accountRepository,
            IEmployeeRepository employeeRepository) {
        this.examRepository = examRepository;
        this.subjectRepository = subjectRepository;
        this.accountRepository = accountRepository;
        this.util = new Util(employeeRepository);
    }

    @Override
    public ExamViewResponseDTO getExamById(long id) throws Exception, NotFoundException {
        ExamViewResponseDTO result = new ExamViewResponseDTO();
        try {
            Exam exam = examRepository.findById(id).get();
            if (exam == null) {
                throw new NotFoundException("Exam id:" + id + " not found");
            }
            result = new ExamViewResponseDTO(exam);
        } catch (NotFoundException nfe) {
            throw nfe;
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
        return result;
    }

    @Override
    public List<ExamViewResponseDTO> GetExam(ExamViewRequestDTO request) throws Exception {
        List<ExamViewResponseDTO> result = new ArrayList<>();
        try {
            Specification<Exam> spec = createSpecificationForGet(request);
            List<Exam> listExams = examRepository.findAll(spec);

            for (Exam exam : listExams) {
                result.add(ExamMapper.INSTANCE.examToViewResponse(exam));
            }

            if (result.isEmpty()) {
                throw new NoSuchElementException("No records");
            }
        } catch (NoSuchElementException e) {
            throw e; // Re-throw custom exception for no records
        } catch (Exception e) {
            throw new Exception("An error occurred while fetching exam records.", e);
        }

        return result;
    }

    @Override
    @Transactional
    public ExamViewResponseDTO createNewExam(ExamCreateRequestDTO entity) throws Exception, NotFoundException {
        ExamViewResponseDTO result = new ExamViewResponseDTO();
        try {
            // Check subject
            Subject subject = checkEntityExistence(subjectRepository.findById(entity.getSubjectId()), "Subject", entity.getSubjectId());

            // Check account
            Account account = checkEntityExistence(accountRepository.findById(Util.getAuthenticatedAccountId()), "Account", Util.getAuthenticatedAccountId());

            //validation exam
            //mapping exam
            Exam exam = ExamMapper.INSTANCE.requestToExam(entity);
            exam.setSubject(subject);
            exam.setCreatedAt(LocalDateTime.now());
            exam.setStatus(true);
            exam.setCreatedBy(account.getAccountId());

            //create new exam
            examRepository.save(exam);
            
            //mapping exam
            result = ExamMapper.INSTANCE.examToViewResponse(exam);
        } catch (NotFoundException ex) {
            throw ex;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return result;
    }

    @Override
    @Transactional
    public ExamViewResponseDTO updateExam(ExamCreateRequestDTO entity) throws Exception, NotFoundException {
        ExamViewResponseDTO result = new ExamViewResponseDTO();
        try {

            // Check subject
            Subject subject = checkEntityExistence(subjectRepository.findById(entity.getSubjectId()), "Subject", entity.getSubjectId());

            //check exist exam
            Exam exam = checkEntityExistence(examRepository.findById(entity.getExamId()), "Exam", entity.getExamId());

            //update exam 
            exam.setExamCode(entity.getExamCode());
            exam.setExamAt(entity.getExamAt());
            exam.setGradingAt(entity.getGradingAt());
            exam.setPublishAt(entity.getPublishAt());
            exam.setSubject(subject);

            //create new exam
            examRepository.save(exam);

            //mapping exam
            result = ExamMapper.INSTANCE.examToViewResponse(exam);
        } catch (NotFoundException nfe) {
            throw nfe;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return result;
    }

    private <T> T checkEntityExistence(Optional<T> entity, String entityName, Long entityId) throws NotFoundException {
        return entity.orElseThrow(() -> new NotFoundException(entityName + " id: " + entityId + " not found"));
    }

// <editor-fold desc="get exam func helper">
    private Specification<Exam> createSpecificationForGet(ExamViewRequestDTO request) {
        Specification<Exam> spec = Specification.where(null);

        if (!request.getSearchString().isBlank()) {
            spec = spec.or(ExamSpecification.hasExamCode(request.getSearchString()))
                    .or(ExamSpecification.hasSemester(request.getSearchString()));
        }

        if (request.getSubjectId() != null) {
            spec.and(ExamSpecification.hasSubjectId(request.getSubjectId()));
        }

        return spec;
    }
// </editor-fold>

    @Override
    public byte[] mergeDataIntoTemplate(String templatePath, Map<String, Object> data) throws Exception {
        // Load the Word document template
        Document doc = new Document(templatePath);

        // Get the mail merge engine
        MailMerge mailMerge = doc.getMailMerge();

        // Prepare arrays of field names and values
        String[] fieldNames = data.keySet().toArray(new String[0]);
        Object[] fieldValues = data.values().toArray();

        // Execute mail merge
        mailMerge.execute(fieldNames, fieldValues);

        // Save the document to a byte array (could save to a file or stream as well)
        byte[] outputBytes;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            doc.save(outputStream, com.aspose.words.SaveFormat.DOCX);
            outputBytes = outputStream.toByteArray();
        }

        return outputBytes;
    }

    @Override
    public void mergeDataIntoWord(String templatePath, String outputPath, Map<DataFieldName, String> data) throws Exception {
        try {
            File file = new File(templatePath);
            // Load the Word template
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(file);

            // Perform the merge
            MailMerger.setMERGEFIELDInOutput(MailMerger.OutputField.REMOVED);
            MailMerger.performMerge(wordMLPackage, data, true);

            // Save the merged document
            wordMLPackage.save(new File(outputPath));
        } catch (Docx4JException e) {
            System.out.println(e.getCause());
            throw e;
        }

    }

    @Override
    public void mergeDataToWord(String templatePath, String outputPath, Map<String, String> data) throws FileNotFoundException, IOException, InvalidFormatException {

        // Step 1: Get the Exam data
        ExamExport exam = getExamToExamExport();

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
    }

    private void addExamQuestions(XWPFDocument document, ExamExport exam) {
        for (ExamQuestionExport question : exam.getQuestions()) {
            // Create a paragraph for the question content and score
            XWPFParagraph questionParagraph = document.createParagraph();
            XWPFRun questionRun = questionParagraph.createRun();
            questionRun.setBold(true);  // Highlight the question
            questionRun.setText("Câu hỏi: " + question.getQuestionContent() + " (" + question.getQuestionScore() + " điểm)");
            questionRun.addCarriageReturn();
            questionRun.setText("Xem thêm tại: " + question.getQuestionURL());
            questionRun.addCarriageReturn();

            // Create numbered list for the barems
            addNumberedBaremList(document, question.getBarems());

            // Add space between questions
            document.createParagraph().createRun().addBreak();
        }
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

    private ExamExport getExamToExamExport() {
        ExamExport result = new ExamExport();
        List<ExamQuestionExport> questions = new ArrayList<>();
        List<ExamBaremExport> barems = new ArrayList<>();
        List<TestCaseExport> testcases = new ArrayList<>();
        TestCaseExport tc1 = new TestCaseExport(
                "Test1", 0.1, "Body", "Response"
        );
        TestCaseExport tc2 = new TestCaseExport(
                "Test2", 0.1, "Body", "Response"
        );

        testcases.add(tc1);
        testcases.add(tc2);

        ExamBaremExport barem = new ExamBaremExport("Barem content", 0.2, testcases);

        barems.add(barem);
        barems.add(barem);

        ExamQuestionExport question = new ExamQuestionExport("Content", 0.4, "Question url", barems);

        questions.add(question);
        questions.add(question);
        try {
            result = new ExamExport(
                    "Code",
                    "Paper code",
                    "Semester",
                    "Subject Code",
                    90,
                    "This is instruction student need to follow it to do the exam",
                    "This is a inpotant information in the exam /n student need to follow it",
                    "This is a database description for the exam",
                    "Database name",
                    "This is the database not use for the exam, this wiil be the one student need to notice",
                    questions
            );

        } catch (Exception e) {
            throw e;
        }

        return result;
    }

// Function to add a numbered list for barems
    private void addNumberedBaremList(XWPFDocument document, List<ExamBaremExport> barems) {
        try {
            // Create a numbering element for numbered list
            CTAbstractNum abstractNum = CTAbstractNum.Factory.newInstance();
            abstractNum.setAbstractNumId(BigInteger.valueOf(0));

            // Create numbering
            XWPFNumbering numbering = document.createNumbering();
            BigInteger abstractNumID = numbering.addAbstractNum(new XWPFAbstractNum(abstractNum));
            BigInteger numID = numbering.addNum(abstractNumID);

            // Add each barem as a numbered item
            for (ExamBaremExport barem : barems) {
                XWPFParagraph paragraph = document.createParagraph();
                paragraph.setNumID(numID);  // Set numbering
                XWPFRun run = paragraph.createRun();
                run.setText(barem.getBaremContent() + " (" + barem.getBaremScore() + " điểm)");
                addNumberedTestCaseList(document, barem.getTestCases());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addNumberedTestCaseList(XWPFDocument document, List<TestCaseExport> testCaseExports) {
        try {
            // Create a numbering element for numbered list
            CTAbstractNum abstractNum = CTAbstractNum.Factory.newInstance();
            abstractNum.setAbstractNumId(BigInteger.valueOf(0));

            // Create numbering
            XWPFNumbering numbering = document.createNumbering();
            BigInteger abstractNumID = numbering.addAbstractNum(new XWPFAbstractNum(abstractNum));
            BigInteger numID = numbering.addNum(abstractNumID);

            // Add each barem as a numbered item
            for (TestCaseExport testCaseExport : testCaseExports) {
                XWPFParagraph paragraph = document.createParagraph();
                paragraph.setNumID(numID);  // Set numbering
                XWPFRun run = paragraph.createRun();
                run.setText(testCaseExport.getTestCaseName() + " (" + String.valueOf(testCaseExport.getMaxScore()) + " điểm)");
                run.addCarriageReturn();
                run.setText("Test case body: " + testCaseExport.getTestcaseBody());
                run.addCarriageReturn();
                run.setText("Test case response: " + testCaseExport.getTestcaseResponse());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String addDatabaseImage(XWPFDocument document, ExamExport exportExam) throws IOException {
        String message = "Image added successfully.";
        String placeholder = "${imagePlaceholder}";
        String imgPath = "C:\\Project\\SEP490\\database-img-test.png";
        byte[] imageBytes = getImageAsByteArray(imgPath);

        try {
            // Iterate through all paragraphs and find the placeholder
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                for (XWPFRun run : paragraph.getRuns()) {
                    String text = run.getText(0);
                    if (text != null && text.contains(placeholder)) {
                        // Replace the placeholder text with the image
                        text = text.replace(placeholder, "");  // Remove the placeholder text
                        run.setText(text, 0);  // Update the run with the modified text

                        // Create a new centered paragraph for the image
                        XWPFParagraph imageParagraph = document.createParagraph();
                        imageParagraph.setAlignment(ParagraphAlignment.CENTER); // Center align the paragraph
                        XWPFRun imageRun = imageParagraph.createRun();

                        // Insert the image at the same location
                        // try (InputStream imageStream = new FileInputStream(imgPath)) {
                        try (ByteArrayInputStream imageInputStream = new ByteArrayInputStream(imageBytes)) {
                            // imageRun.addBreak();  // Optional: Add a break before the image
                            // imageRun.addPicture(
                            //         imageStream,
                            //         XWPFDocument.PICTURE_TYPE_PNG, // Use appropriate image type
                            //         imgPath,
                            //         Units.toEMU(200), // Width in EMUs
                            //         Units.toEMU(150) // Height in EMUs
                            // );
                            imageRun.addBreak();  // Optional: Add a break before the image
                            imageRun.addPicture(
                                    imageInputStream,
                                    XWPFDocument.PICTURE_TYPE_PNG, // Specify the correct image type (PNG in this example)
                                    "image.png", // Image name (for internal Word reference, not the file system)
                                    Units.toEMU(200), // Width in EMUs
                                    Units.toEMU(150) // Height in EMUs
                            );
                        } catch (FileNotFoundException fnfe) {
                            message = "Error: Image file not found.";
                            fnfe.printStackTrace();
                        } catch (IOException e) {
                            message = "Error adding image to document.";
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (InvalidFormatException e) {
            message = "An error occurred while processing the document.";
            e.printStackTrace();
        }

        return message;
    }

    public static byte[] getImageAsByteArray(String pathToImage) throws IOException {
        return Files.readAllBytes(Paths.get(pathToImage));
    }

}
