package com.CodeEvalCrew.AutoScore.services.exam_service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.model.fields.merge.MailMerger;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.mappers.ExamMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamCreateRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamExport;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam.ExamViewRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamQuestion.ExamQuestionExport;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamViewResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamWithPapersDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.models.Entity.Account_Organization;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Exam_Type_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Notification_Type_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Organization_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Database;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Question;
import com.CodeEvalCrew.AutoScore.models.Entity.Notification;
import com.CodeEvalCrew.AutoScore.models.Entity.Organization;
import com.CodeEvalCrew.AutoScore.models.Entity.Organization_Subject;
import com.CodeEvalCrew.AutoScore.models.Entity.Semester;
import com.CodeEvalCrew.AutoScore.models.Entity.Subject;
import com.CodeEvalCrew.AutoScore.repositories.account_organization_repository.AccountOrganizationRepository;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IEmployeeRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamQuestionRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamRepository;
import com.CodeEvalCrew.AutoScore.repositories.examdatabase_repository.IExamDatabaseRepository;
import com.CodeEvalCrew.AutoScore.repositories.notification_repository.NotificationRepository;
import com.CodeEvalCrew.AutoScore.repositories.semester_repository.SemesterRepository;
import com.CodeEvalCrew.AutoScore.repositories.subject_repository.ISubjectRepository;
import com.CodeEvalCrew.AutoScore.repositories.subject_repository.SubjectOrgenizationRepository;
import com.CodeEvalCrew.AutoScore.specification.ExamDatabaseSpecification;
import com.CodeEvalCrew.AutoScore.specification.ExamSpecification;
import com.CodeEvalCrew.AutoScore.utils.SendNotificationUtil;
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
    private final SubjectOrgenizationRepository subjectOrganOrgenizationRepository;
    @Autowired
    private final SemesterRepository semesterRepository;
    @Autowired
    private final IAccountRepository accountRepository;
    @Autowired
    private final IExamPaperRepository examPaperRepository;
    @Autowired
    private final IExamDatabaseRepository examDatabaseRepository;
    @Autowired
    private final IExamQuestionRepository examQuestionRepository;
    @Autowired
    private final AccountOrganizationRepository accountOrganizationRepository;
    @Autowired
    private final NotificationRepository notificationRepository;
    private final SendNotificationUtil notiUtil;

    public ExamService(IExamRepository examRepository,
            ISubjectRepository subjectRepository,
            IAccountRepository accountRepository,
            IExamPaperRepository examPaperRepository,
            IExamQuestionRepository examQuestionRepository,
            IExamDatabaseRepository examDatabaseRepository,
            SemesterRepository semesterRepository,
            IEmployeeRepository employeeRepository,
            SubjectOrgenizationRepository subjectOrganOrgenizationRepository,
            NotificationRepository notificationRepository,
            SendNotificationUtil notiUtil,
            AccountOrganizationRepository accountOrganizationRepository) {
        this.examRepository = examRepository;
        this.subjectRepository = subjectRepository;
        this.accountRepository = accountRepository;
        this.examPaperRepository = examPaperRepository;
        this.examDatabaseRepository = examDatabaseRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.semesterRepository = semesterRepository;
        this.accountOrganizationRepository = accountOrganizationRepository;
        this.subjectOrganOrgenizationRepository = subjectOrganOrgenizationRepository;
        this.notificationRepository = notificationRepository;
        this.notiUtil = notiUtil;
    }

    @Override
    public ExamViewResponseDTO getExamById(long id) throws Exception, NotFoundException {
        ExamViewResponseDTO result = new ExamViewResponseDTO();
        try {
            Exam exam = examRepository.findById(id).get();
            if (exam == null) {
                throw new NotFoundException("Exam id:" + id + " not found");
            }
            result = ExamMapper.INSTANCE.examToViewResponse(exam);
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

            Long curAccountID = Util.getAuthenticatedAccountId();
            Account curAccount = accountRepository.findById(curAccountID).get();
            if (curAccount == null) {
                throw new NoSuchElementException("Account not fould");
            }
            Organization curOrg = new Organization();
            Set<Organization> orgs = Util.getOrganizations();
            for (Organization org : orgs) {
                if (org.getType() == Organization_Enum.CAMPUS) {
                    curOrg = org;
                }
            }
            List<Organization_Subject> orgSubs = subjectOrganOrgenizationRepository.findByOrganization_OrganizationId(curOrg.getOrganizationId());
            List<Subject> subs = new ArrayList<>();
            for (Organization_Subject orgSub : orgSubs) {
                subs.add(orgSub.getSubject());
            }

            if (curAccount.getRole().getRoleName().equals("ADMIN")) {
                for (Exam exam : listExams) {
                    result.add(ExamMapper.INSTANCE.examToViewResponse(exam));
                }
            } else {
                for (Exam exam : listExams) {
                    for (Subject subject : subs) {
                        if (exam.getSubject().getSubjectId().equals(subject.getSubjectId())) {
                            result.add(ExamMapper.INSTANCE.examToViewResponse(exam));
                        }
                    }
                }
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
            //Check semester
            Semester semester = checkEntityExistence(semesterRepository.findById(entity.getSemesterId()), "Semester", entity.getSemesterId());
            Organization org = new Organization();
            Set<Organization> orgs = Util.getOrganizations();
            for (Organization organization : orgs) {
                if (organization.getType().equals(Organization_Enum.CAMPUS)) {
                    org = organization;
                }
            }
            //mapping exam
            Exam exam = ExamMapper.INSTANCE.requestToExam(entity);
            exam.setSubject(subject);
            exam.setCreatedAt(LocalDateTime.now());
            exam.setStatus(true);
            exam.setType(Exam_Type_Enum.EXAM);
            exam.setCreatedBy(account.getAccountId());
            exam.setSemester(semester);
            //create new exam
            exam = examRepository.save(exam);
            //Noti
            Notification noti = new Notification(null, "New exam", "New exam has create in your campus", "/exams", Notification_Type_Enum.NOTIFICATION, null);
            Notification newNoti = notificationRepository.save(noti);
            notiUtil.sendNotiToAllAccountIncampus(newNoti, org);

            //mapping exam
            result = ExamMapper.INSTANCE.examToViewResponse(exam);
        } catch (NotFoundException ex) {
            throw ex;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception(e.getMessage());
        }
        return result;
    }

    @Override
    @Transactional
    public ExamViewResponseDTO updateExam(ExamCreateRequestDTO entity, Long id) throws Exception, NotFoundException {
        ExamViewResponseDTO result = new ExamViewResponseDTO();
        try {
            //check exist exam
            Exam exam = checkEntityExistence(examRepository.findById(id), "Exam", id);
            // Check subject
            Subject subject = checkEntityExistence(subjectRepository.findById(entity.getSubjectId()), "Subject", entity.getSubjectId());
            //Check semester
            Semester semester = checkEntityExistence(semesterRepository.findById(entity.getSemesterId()), "Semester", entity.getSemesterId());
            //update exam 
            exam.setExamCode(entity.getExamCode());
            exam.setExamAt(entity.getExamAt());
            exam.setGradingAt(entity.getGradingAt());
            exam.setPublishAt(entity.getPublishAt());
            exam.setSubject(subject);
            exam.setSemester(semester);
            //save exam
            examRepository.save(exam);
            // mapping exam to return
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
    public void mergeDataToWord(String templatePath, String outputPath, Map<String, String> data) throws Exception {
        ExamExport exam = new ExamExport();
        // Step 1: Get the Exam data
        try {
            exam = getExamToExamExport(1l);
        } catch (NotFoundException ex) {

        }

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
            questionRun.setText("Question: " + question.getQuestionContent());

            // Create numbered list for the barems
            // addNumberedBaremList(document, question.getBarems());
            // Add space between questions
            document.createParagraph().createRun();
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

    public static byte[] getImageAsByteArray(String pathToImage) throws IOException {
        String imgPath = "C:\\Project\\SEP490\\database-img-test.png";
        return Files.readAllBytes(Paths.get(imgPath));
    }

    private List<ExamQuestionExport> getListExamQuestionExport(Long examPaperId) throws NoSuchElementException {
        List<ExamQuestionExport> result = new ArrayList<>();

        List<Exam_Question> listQuestions = examQuestionRepository.getByExamPaperExamPaperId(examPaperId);

        if (listQuestions.isEmpty()) {
            throw new NoSuchElementException("No question found");
        }

        for (Exam_Question examQuestion : listQuestions) {
            ExamQuestionExport export = new ExamQuestionExport();
            export.setQuestionContent(examQuestion.getQuestionContent());
            result.add(export);
        }

        return result;
    }

    @Override
    public List<ExamWithPapersDTO> getExamWithUsedPapers() {
        return examPaperRepository.findByIsUsedTrueOrderByCreatedAtDesc()
                .stream()
                .map(examPaper -> new ExamWithPapersDTO(
                examPaper.getExam().getExamCode(),
                examPaper.getExamPaperCode(),
                examPaper.getExamPaperId()
        ))
                .collect(Collectors.toList());
    }

    private String checkCampusForAccount(Long accountId) {

        List<Account_Organization> accountOrganizations = accountOrganizationRepository.findByAccount_AccountId(accountId);

        for (Account_Organization accountOrg : accountOrganizations) {
            Organization organization = accountOrg.getOrganization();
            if (organization.getType() == Organization_Enum.CAMPUS) {
                return organization.getName();
            }
        }

        return null;
    }

    @Override
    public long countExamsByTypeAndCampus() {

        Long authenticatedUserId = Util.getAuthenticatedAccountId();

        String userCampus = checkCampusForAccount(authenticatedUserId);

        if (userCampus == null) {
            throw new IllegalArgumentException("Authenticated user does not belong to any CAMPUS.");
        }

        List<Exam> exams = examRepository.findByType(Exam_Type_Enum.EXAM);

        long count = exams.stream()
                .filter(exam -> checkCampusForAccount(exam.getCreatedBy()).equals(userCampus))
                .count();

        return count;
    }

    @Override
    public long countExamsByTypeAndGradingAt() {
        Long authenticatedUserId = Util.getAuthenticatedAccountId();

        String userCampus = checkCampusForAccount(authenticatedUserId);

        if (userCampus == null) {
            throw new IllegalArgumentException("Authenticated user does not belong to any CAMPUS.");
        }

        List<Exam> exams = examRepository.findByType(Exam_Type_Enum.EXAM);

        long count = exams.stream()
                .filter(exam -> exam.getGradingAt().isAfter(LocalDateTime.now())
                && checkCampusForAccount(exam.getCreatedBy()).equals(userCampus))
                .count();

        return count;
    }

    @Override
    public long countExamsByGradingAtPassed() {
        Long authenticatedUserId = Util.getAuthenticatedAccountId();

        String userCampus = checkCampusForAccount(authenticatedUserId);

        if (userCampus == null) {
            throw new IllegalArgumentException("Authenticated user does not belong to any CAMPUS.");
        }

        List<Exam> exams = examRepository.findByType(Exam_Type_Enum.EXAM);

        long count = exams.stream()
                .filter(exam -> exam.getGradingAt().isBefore(LocalDateTime.now())
                && checkCampusForAccount(exam.getCreatedBy()).equals(userCampus))
                .count();

        return count;
    }

    @Override
    public Map<String, Long> countExamsByGradingAtPassedAndSemester(int year) {
        Long authenticatedUserId = Util.getAuthenticatedAccountId();

        // Kiểm tra campus của authenticatedUserId
        String userCampus = checkCampusForAccount(authenticatedUserId);

        if (userCampus == null) {
            throw new IllegalArgumentException("Authenticated user does not belong to any CAMPUS.");
        }

        // Lấy tất cả các Exam có type là EXAM
        List<Exam> exams = examRepository.findByType(Exam_Type_Enum.EXAM);

        // Tạo map để lưu số lượng exam cho từng kỳ
        Map<String, Long> examCounts = new HashMap<>();
        examCounts.put("Spring", 0L);
        examCounts.put("Summer", 0L);
        examCounts.put("Fall", 0L);

        // Lọc các Exam có gradingAt đã vượt qua thời gian hiện tại
        for (Exam exam : exams) {
            if (exam.getGradingAt().isBefore(LocalDateTime.now()) // gradingAt đã vượt qua thời gian hiện tại
                    && checkCampusForAccount(exam.getCreatedBy()).equals(userCampus) // Kiểm tra campus của createdBy
                    && exam.getGradingAt().getYear() == year) { // Kiểm tra năm của gradingAt

                // Lọc theo kỳ (Spring: tháng 1-4, Summer: tháng 5-8, Fall: tháng 9-12)
                int month = exam.getGradingAt().getMonthValue();
                if (month >= 1 && month <= 4) {
                    examCounts.put("Spring", examCounts.get("Spring") + 1);
                } else if (month >= 5 && month <= 8) {
                    examCounts.put("Summer", examCounts.get("Summer") + 1);
                } else if (month >= 9 && month <= 12) {
                    examCounts.put("Fall", examCounts.get("Fall") + 1);
                }
            }
        }

        return examCounts;
    }

}
