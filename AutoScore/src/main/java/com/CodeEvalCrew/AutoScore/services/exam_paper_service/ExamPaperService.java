package com.CodeEvalCrew.AutoScore.services.exam_paper_service;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.mappers.ExamPaperMapper;
import com.CodeEvalCrew.AutoScore.mappers.ImportantMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamPaper.ExamPaperCreateRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamPaper.ExamPaperToExamRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamPaper.ExamPaperViewRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Semester.SemesterView;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamPaperFilePostmanResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamPaperView;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ImportantView;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.NewmanResult;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Exam_Status_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Exam_Type_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Database;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Question;
import com.CodeEvalCrew.AutoScore.models.Entity.Important;
import com.CodeEvalCrew.AutoScore.models.Entity.Important_Exam_Paper;
import com.CodeEvalCrew.AutoScore.models.Entity.Log;
import com.CodeEvalCrew.AutoScore.models.Entity.Postman_For_Grading;
import com.CodeEvalCrew.AutoScore.models.Entity.Subject;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamQuestionRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamRepository;
import com.CodeEvalCrew.AutoScore.repositories.examdatabase_repository.IExamDatabaseRepository;
import com.CodeEvalCrew.AutoScore.repositories.important_repository.ImportantExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.important_repository.ImportantRepository;
import com.CodeEvalCrew.AutoScore.repositories.log_repository.LogRepository;
import com.CodeEvalCrew.AutoScore.repositories.postman_for_grading.PostmanForGradingRepository;
import com.CodeEvalCrew.AutoScore.repositories.subject_repository.ISubjectRepository;
import com.CodeEvalCrew.AutoScore.specification.ExamPaperSpecification;
import com.CodeEvalCrew.AutoScore.utils.PathUtil;
import com.CodeEvalCrew.AutoScore.utils.Util;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.transaction.Transactional;

@Service
public class ExamPaperService implements IExamPaperService {

    @Autowired
    private final IExamPaperRepository examPaperRepository;
    @Autowired
    private final IExamRepository examRepository;
    @Autowired
    private final ImportantRepository importantRepository;
    @Autowired
    private PostmanForGradingRepository postmanForGradingRepository;
    @Autowired
    private IExamQuestionRepository examQuestionRepository;
    @Autowired
    private final ImportantExamPaperRepository importantExamPaperRepository;
    @Autowired
    private final ObjectMapper objectMapper;
    @Autowired
    private ISubjectRepository subjectRepository;
    @Autowired
    private LogRepository logRepository;
    @Autowired
    private PathUtil pathUtil;
    @Autowired
    private IExamDatabaseRepository examDatabaseRepository;

    public ExamPaperService(IExamPaperRepository examPaperRepository,
            IExamRepository examRepository,
            ImportantRepository importantRepository,
            ImportantExamPaperRepository importantExamPaperRepository,
            ObjectMapper objectMapper) {
        this.examPaperRepository = examPaperRepository;
        this.examRepository = examRepository;
        this.objectMapper = objectMapper;
        this.importantRepository = importantRepository;
        this.importantExamPaperRepository = importantExamPaperRepository;
    }

    private void saveLog(Long examPaperId, String actionDetail) {

        Optional<Exam_Paper> optionalExamPaper = examPaperRepository.findById(examPaperId);
        if (optionalExamPaper.isEmpty()) {
            throw new IllegalArgumentException("Exam Paper with ID " + examPaperId + " does not exist.");
        }

        Exam_Paper examPaper = optionalExamPaper.get();
        Log log = examPaper.getLog();

        if (log == null) {
            log = new Log();
            log.setExamPaper(examPaper);
            log.setAllData(actionDetail);
        } else {

            String updatedData = log.getAllData() == null ? "" : log.getAllData() + ", ";
            log.setAllData(updatedData + actionDetail);
        }

        logRepository.save(log);
    }

    @Override
    public ExamPaperView getById(Long id) throws NotFoundException {
        try {
            Exam_Paper examPaper = checkEntityExistence(examPaperRepository.findById(id), "Exam Paper", id);

            ExamPaperView examPaperView = ExamPaperMapper.INSTANCE.examPAperToView(examPaper);
            Set<ImportantView> set = new HashSet<>();
            for (Important_Exam_Paper a : examPaper.getImportants()) {
                Important important = checkEntityExistence(
                        importantRepository.findById(a.getImportant().getImportantId()), "Improtant",
                        a.getImportant().getImportantId());
                ImportantView view = ImportantMapper.INSTANCE.formImportantToView(important);
                set.add(view);
            }

            if (examPaper.getExam() != null) {
                Exam exam = examPaper.getExam();
                SemesterView semesterView = new SemesterView(exam.getSemester().getSemesterId(), exam.getSemester().getSemesterCode(), exam.getSemester().getSemesterName());
                examPaperView.setSemester(semesterView);
            }

            examPaperView.setImportants(set);

            return examPaperView;
        } catch (NotFoundException nfe) {
            throw nfe;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    @Override
    public List<ExamPaperView> getList(ExamPaperViewRequest request) throws NotFoundException, NoSuchElementException {
        List<ExamPaperView> result = new ArrayList<>();
        try {
            // check exam
            Exam exam = checkEntityExistence(examRepository.findById(request.getExamId()), "Exam", request.getExamId());
            // crete spec
            Specification<Exam_Paper> spec = ExamPaperSpecification.hasForeignKey(request.getExamId(), "exam",
                    "examId");
            spec.and(ExamPaperSpecification.hasTrueStatus());
            //check exam
            List<Exam_Paper> listEntities = examPaperRepository.findAll(spec);

            if (listEntities.isEmpty()) {
                throw new NoSuchElementException("No exam paper found");
            }

            for (Exam_Paper exam_Paper : listEntities) {
                ExamPaperView examPaperView = ExamPaperMapper.INSTANCE.examPAperToView(exam_Paper);
                Set<ImportantView> set = new HashSet<>();
                for (Important_Exam_Paper a : exam_Paper.getImportants()) {
                    Important important = checkEntityExistence(
                            importantRepository.findById(a.getImportant().getImportantId()), "Improtant",
                            a.getImportant().getImportantId());
                    ImportantView view = ImportantMapper.INSTANCE.formImportantToView(important);
                    set.add(view);
                }
                examPaperView.setImportants(set);
                SemesterView semesterView = new SemesterView(exam.getSemester().getSemesterId(), exam.getSemester().getSemesterCode(), exam.getSemester().getSemesterName());
                examPaperView.setSemester(semesterView);

                result.add(examPaperView);
            }

            return result;
        } catch (NotFoundException | NoSuchElementException nfe) {
            throw nfe;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    @Override
    @Transactional
    public ExamPaperView createNewExamPaper(ExamPaperCreateRequest request) throws NotFoundException {
        try {
            // check Exam
            Exam exam = checkEntityExistence(examRepository.findById(request.getExamId()), "Exam", request.getExamId());

            Subject subject = checkEntityExistence(subjectRepository.findById(request.getSubjectId()), "Subject",
                    request.getSubjectId());

            // set to add
            Set<Important_Exam_Paper> importants = new HashSet<>();

            // mapping
            Exam_Paper examPaper = ExamPaperMapper.INSTANCE.requestToExamPaper(request);

            // check important to add to exam paper
            Set<ImportantView> set = new HashSet<>();
            for (Long importantId : request.getImportantIdList()) {
                Important important = checkEntityExistence(importantRepository.findById(importantId), "Important",
                        importantId);

                Important_Exam_Paper importantExamPaper = new Important_Exam_Paper(null, Exam_Status_Enum.ACTIVE,
                        important, examPaper);

                importants.add(importantExamPaper);
                ImportantView view = ImportantMapper.INSTANCE.formImportantToView(important);
                set.add(view);
            }
            // update side in4
            examPaper.setExam(exam);
            examPaper.setImportants(importants);
            examPaper.setSubject(subject);
            examPaper.setStatus(Exam_Status_Enum.DRAFT);
            examPaper.setDuration(request.getDuration());
            examPaper.setCreatedAt(Util.getCurrentDateTime());
            examPaper.setCreatedBy(Util.getAuthenticatedAccountId());
            examPaper.setIsUsed(false);

            examPaperRepository.save(examPaper);

            ExamPaperView examPaperView = ExamPaperMapper.INSTANCE.examPAperToView(examPaper);
            if (examPaper.getExam() != null) {
                SemesterView semesterView = new SemesterView(exam.getSemester().getSemesterId(), exam.getSemester().getSemesterCode(), exam.getSemester().getSemesterName());
                examPaperView.setSemester(semesterView);
            }
            examPaperView.setImportants(set);

            return examPaperView;
        } catch (NotFoundException nfe) {
            throw nfe;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    @Override
    @Transactional
    public ExamPaperView updateExamPaper(Long id, ExamPaperCreateRequest request) throws NotFoundException {
        try {
            // check ExamPaper
            Exam_Paper examPaper = checkEntityExistence(examPaperRepository.findById(id), "Exam Paper", id);

            // check Exam
            Exam exam = checkEntityExistence(examRepository.findById(request.getExamId()), "Exam", request.getExamId());

            importantExamPaperRepository.deleteByExamPaper_ExamPaperId(id);
            Set<Important_Exam_Paper> importants = new HashSet<>();

            Set<ImportantView> set = new HashSet<>();
            for (Long importantId : request.getImportantIdList()) {
                Important important = checkEntityExistence(importantRepository.findById(importantId), "Important",
                        importantId);

                Important_Exam_Paper importantExamPaper = new Important_Exam_Paper(null, Exam_Status_Enum.ACTIVE,
                        important, examPaper);

                importants.add(importantExamPaper);
                ImportantView view = ImportantMapper.INSTANCE.formImportantToView(important);
                set.add(view);
            }

            // update side in4
            examPaper.setExamPaperCode(request.getExamPaperCode());
            examPaper.setExam(exam);
            examPaper.setImportants(importants);
            examPaper.setInstruction(request.getInstruction());
            examPaper.setDuration(request.getDuration());
            examPaper.setStatus(Exam_Status_Enum.DRAFT);
            examPaper.setUpdatedAt(Util.getCurrentDateTime());
            examPaper.setUpdatedBy(Util.getAuthenticatedAccountId());

            examPaperRepository.save(examPaper);

            ExamPaperView examPaperView = ExamPaperMapper.INSTANCE.examPAperToView(examPaper);
            if (examPaper.getExam() != null) {
                SemesterView semesterView = new SemesterView(exam.getSemester().getSemesterId(), exam.getSemester().getSemesterCode(), exam.getSemester().getSemesterName());
                examPaperView.setSemester(semesterView);
            }
            examPaperView.setImportants(set);

            return examPaperView;
        } catch (NotFoundException nfe) {
            throw nfe;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    @Override
    public ExamPaperView deleteExamPaper(Long id) throws NotFoundException {
        try {

            Exam_Paper examPaper = checkEntityExistence(examPaperRepository.findById(id), "Exam Paper", id);

            examPaper.setStatus(Exam_Status_Enum.UNACTIVE);
            examPaper.setDeletedAt(Util.getCurrentDateTime());
            examPaper.setDeletedBy(Util.getAuthenticatedAccountId());

            return ExamPaperMapper.INSTANCE.examPAperToView(examPaper);
        } catch (NotFoundException nfe) {
            throw nfe;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    private <T> T checkEntityExistence(Optional<T> entity, String entityName, Long entityId) throws NotFoundException {
        return entity.orElseThrow(() -> new NotFoundException(entityName + " id: " + entityId + " not found"));
    }

    private java.io.File convertBytesToFile(byte[] fileData, String fileName) throws Exception {
        java.io.File tempFile = new java.io.File(System.getProperty("java.io.tmpdir") + "\\" + fileName);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(fileData);
        }
        return tempFile;
    }

    private boolean isValidJson(java.io.File file) {
        try {
            objectMapper.readTree(file); // Try to parse the JSON
            return true; // JSON is valid
        } catch (Exception e) {
            return false; // JSON is invalid
        }
    }

    public void updatePostmanForGradingStatus(Long examPaperId) {
        // Fetch all Postman_For_Grading entities for the given examPaperId
        List<Postman_For_Grading> postmanForGradingList = postmanForGradingRepository
                .findByExamPaper_ExamPaperId(examPaperId);

        // Iterate through the list and update the fields
        for (Postman_For_Grading postman : postmanForGradingList) {
            postman.setStatus(false); // Set status to false
            postman.setExamQuestion(null); // Set examQuestionId to null
            if (postman.getGherkinScenario() != null) {
                postman.setGherkinScenario(null);
            }
        }

        // Save the updated entities back to the database
        postmanForGradingRepository.saveAll(postmanForGradingList);
    }

    private void validateUniqueItemNames(String fileContent) throws Exception {
        JSONObject json = new JSONObject(fileContent);
        JSONArray items = json.getJSONArray("item");

        Set<String> existingNames = new HashSet<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            String name = item.getString("name");

            if (!existingNames.add(name)) {
                throw new Exception("Duplicate item name detected: " + name);
            }
        }
    }

    @Override
    public void importPostmanCollections(Long examPaperId, List<MultipartFile> files) throws Exception {

        Long authenticatedUserId = Util.getAuthenticatedAccountId();
        LocalDateTime time = Util.getCurrentDateTime();

        try {
            Exam_Paper examPaper = examPaperRepository.findById(examPaperId)
                    .orElseThrow(() -> new NotFoundException("Exam Paper not found for ID: " + examPaperId));

            // List<PostmanFunctionInfo> functionInfoList =
            // getPostmanFunctionInfoByExamPaperId(examPaperId);
            // List<String> allNewmanFunctionNames = new ArrayList<>();
            for (MultipartFile file : files) {

                byte[] fileData = file.getBytes();

                String fileContent = new String(fileData, StandardCharsets.UTF_8);

                validateUniqueItemNames(fileContent);

                updatePostmanForGradingStatus(examPaperId);

                java.io.File tempFile = convertBytesToFile(fileData, file.getOriginalFilename());

                if (!isValidJson(tempFile)) {
                    throw new Exception("File " + file.getOriginalFilename() + " contains invalid JSON.");
                }

                String newmanOutput = runNewmanTest(tempFile);

                NewmanResult newmanResult = handleNewmanResult(newmanOutput, examPaperId);

                updateFileCollectionPostmanForGrading(fileContent.getBytes(StandardCharsets.UTF_8), examPaperId);

                updateExamQuestionInPostman(fileContent.getBytes(StandardCharsets.UTF_8), examPaperId);

                examPaper.setFileCollectionPostman(null);
                examPaper.setFileCollectionPostman(fileContent.getBytes(StandardCharsets.UTF_8));
                examPaper.setIsComfirmFile(false);
                examPaper.setLogRunPostman(null);
                examPaper.setLogRunPostman(newmanOutput);

                examPaperRepository.save(examPaper);

                Files.deleteIfExists(tempFile.toPath());
                saveLog(examPaper.getExamPaperId(),
                        "Account [" + authenticatedUserId + "] [Import file postman successfully] at [" + time + "]");
            }

        } catch (NotFoundException e) {
            throw new Exception("Exam Paper with ID " + examPaperId + " not found.", e);
        } catch (Exception e) {
            throw new Exception("Failed to import files: " + e.getMessage(), e);
        }
    }

    public void updateExamQuestionInPostman(byte[] fileContent, Long examPaperId) throws Exception {
        try {
            String jsonContent = new String(fileContent, StandardCharsets.UTF_8);
            JSONObject collectionJson = new JSONObject(jsonContent);

            JSONArray items = collectionJson.getJSONArray("item");

            List<Postman_For_Grading> postmanForGradingList = postmanForGradingRepository
                    .findByExamPaper_ExamPaperIdAndStatusTrueOrderByOrderPriorityAsc(examPaperId);

            List<Exam_Question> examQuestions = examQuestionRepository.findByExamPaperId(examPaperId);

            for (Postman_For_Grading postmanFunction : postmanForGradingList) {
                String functionName = postmanFunction.getPostmanFunctionName();

                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    String itemName = item.getString("name");

                    if (itemName.equals(functionName)) {
                        JSONObject request = item.getJSONObject("request");
                        String httpMethod = request.getString("method").toUpperCase();

                        JSONObject urlObject = request.getJSONObject("url");
                        JSONArray pathArray = urlObject.getJSONArray("path");
                        String actualPath = "/" + String.join("/",
                                pathArray.toList().stream().map(Object::toString).toArray(String[]::new));

                        List<Exam_Question> matchingQuestions = examQuestions.stream()
                                .filter(question -> question.getHttpMethod().equals(httpMethod)
                                && isPathMatchingWithDynamicSegments(question.getEndPoint(), actualPath))
                                .collect(Collectors.toList());

                        if (matchingQuestions.size() == 1) {
                            postmanFunction.setExamQuestion(matchingQuestions.get(0));

                            postmanForGradingRepository.save(postmanFunction);
                        } else if (matchingQuestions.size() > 1) {

                        }

                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("Failed to update Postman functions: " + e.getMessage(), e);
        }
    }

    private boolean isPathMatchingWithDynamicSegments(String endpointPattern, String actualPath) {
        // Replace dynamic `{param}` parameters in endpoint with regex
        String regexPattern = endpointPattern.replaceAll("\\{[^/]+\\}", "[^/]+");
        return actualPath.matches(regexPattern);
    }

    private void updateFileCollectionPostmanForGrading(byte[] fileData, Long examPaperId) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode collectionJson = objectMapper.readTree(new String(fileData, StandardCharsets.UTF_8));

        if (!collectionJson.has("item")) {
            throw new IllegalArgumentException("Invalid Postman collection format: Missing 'item' field.");
        }

        ArrayNode items = (ArrayNode) collectionJson.get("item");

        List<Postman_For_Grading> postmanForGradingList = postmanForGradingRepository
                .findByExamPaper_ExamPaperIdAndStatusTrue(examPaperId);

        for (Postman_For_Grading postman : postmanForGradingList) {
            String functionName = postman.getPostmanFunctionName();

            ArrayNode matchingItems = findMatchingItems(items, functionName, objectMapper);

            if (matchingItems.size() > 0) {
                ObjectNode updatedCollection = objectMapper.createObjectNode();
                updatedCollection.set("info", collectionJson.get("info"));
                updatedCollection.set("item", matchingItems);

                postman.setFileCollectionPostman(objectMapper.writeValueAsBytes(updatedCollection));
            } else {
                System.out.println("No matching items found for function: " + functionName);
            }
        }

        postmanForGradingRepository.saveAll(postmanForGradingList);

    }

    private ArrayNode findMatchingItems(ArrayNode items, String functionName, ObjectMapper objectMapper) {
        ArrayNode matchingItems = objectMapper.createArrayNode();
        for (JsonNode item : items) {
            if (item.has("name") && functionName.equals(item.get("name").asText())) {
                matchingItems.add(item);
            }
        }
        return matchingItems;
    }

    private String runNewmanTest(java.io.File file) throws Exception {
        StringBuilder output = new StringBuilder();
        String newmanCmdPath = pathUtil.getNewmanCmdPath();
        try {

            ProcessBuilder processBuilder = new ProcessBuilder(newmanCmdPath, "run",
                    "\"" + file.getAbsolutePath() + "\"");
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Failed to run Newman test: " + e.getMessage(), e);
        }

        return output.toString();
    }

    private NewmanResult handleNewmanResult(String newmanOutput, Long examPaperId)
            throws NotFoundException {

        NewmanResult result = parseNewmanOutput(newmanOutput);

        List<String> functionNamesFromNewman = result.getFunctionNames();

        for (int i = 0; i < functionNamesFromNewman.size(); i++) {
            String functionName = functionNamesFromNewman.get(i);
            Long newTotalPmTest = (long) result.getTotalPmTests().get(i);
            createNewPostmanForGrading(functionName, newTotalPmTest, examPaperId);
        }

        return result;
    }

    private void createNewPostmanForGrading(String functionName, Long totalPmTest, Long examPaperId)
            throws NotFoundException {

        Exam_Paper examPaper = examPaperRepository.findById(examPaperId)
                .orElseThrow(() -> new NotFoundException("Exam Paper not found for ID: " + examPaperId));

        Postman_For_Grading newPostmanForGrading = new Postman_For_Grading();
        newPostmanForGrading.setPostmanFunctionName(functionName);
        newPostmanForGrading.setTotalPmTest(totalPmTest);
        newPostmanForGrading.setExamPaper(examPaper);
        newPostmanForGrading.setStatus(true);

        postmanForGradingRepository.save(newPostmanForGrading);
    }

    private NewmanResult parseNewmanOutput(String newmanOutput) {
        NewmanResult result = new NewmanResult();
        List<String> functionNames = new ArrayList<>();
        List<Integer> totalPmTests = new ArrayList<>();
        String[] lines = newmanOutput.split("\n");
        String currentFunctionName = null;
        int currentFunctionTestCount = 0;

        boolean isParsingFunctions = true;

        for (String line : lines) {

            if (line.trim().startsWith("┌") || line.trim().startsWith("│") || line.trim().startsWith("└")) {
                isParsingFunctions = false;
            }

            if (!isParsingFunctions) {
                break;
            }

            if (line.startsWith("→")) {
                if (currentFunctionName != null) {
                    functionNames.add(currentFunctionName);
                    totalPmTests.add(currentFunctionTestCount);
                }
                currentFunctionName = line.substring(2).trim();
                currentFunctionTestCount = 0;
            }

            if (line.trim().matches("^\\d+.*") || line.trim().startsWith("√")) {
                currentFunctionTestCount++;
            }
        }

        if (currentFunctionName != null) {
            functionNames.add(currentFunctionName);
            totalPmTests.add(currentFunctionTestCount);
        }

        result.setFunctionNames(functionNames);
        result.setTotalPmTests(totalPmTests);
        return result;
    }

    @Override
    public byte[] exportPostmanCollection(Long examPaperId) throws Exception {

        Long authenticatedUserId = Util.getAuthenticatedAccountId();
        LocalDateTime time = Util.getCurrentDateTime();

        Exam_Paper examPaper = examPaperRepository.findById(examPaperId)
                .orElseThrow(() -> new Exception("Exam Paper not found with ID: " + examPaperId));

        saveLog(examPaper.getExamPaperId(), "Account [" + authenticatedUserId
                + "] [Export file postman collection successfully] at [" + time + "]");

        return examPaper.getFileCollectionPostman();

    }

    @Override
    public List<ExamPaperView> getAllExamNotUsed() throws NotFoundException, Exception {
        List<ExamPaperView> result = new ArrayList<>();
        try {
            Specification<Exam_Paper> spec = ExamPaperSpecification.isUsedFalse();

            List<Exam_Paper> listExamPaper = examPaperRepository.findAll(spec);

            if (listExamPaper.isEmpty()) {
                throw new NoSuchElementException("No exam paper found");
            }

            for (Exam_Paper examPaper : listExamPaper) {
                ExamPaperView examPaperView = ExamPaperMapper.INSTANCE.examPAperToView(examPaper);

                Set<ImportantView> set = new HashSet<>();
                for (Important_Exam_Paper a : examPaper.getImportants()) {
                    Important important = checkEntityExistence(
                            importantRepository.findById(a.getImportant().getImportantId()), "Improtant",
                            a.getImportant().getImportantId());
                    ImportantView view = ImportantMapper.INSTANCE.formImportantToView(important);
                    set.add(view);
                }
                examPaperView.setImportants(set);

                result.add(examPaperView);
            }

            return result;
        } catch (NoSuchElementException | NotFoundException e) {
            throw e;
        }
    }

    @Override
    public ExamPaperView createNewExamPaperNotUsed(ExamPaperCreateRequest request) throws NotFoundException {
        try {
            // set to add
            Set<Important_Exam_Paper> importants = new HashSet<>();

            // mapping
            Exam_Paper examPaper = ExamPaperMapper.INSTANCE.requestToExamPaper(request);
            Subject subject = checkEntityExistence(subjectRepository.findById(request.getSubjectId()), "Subject",
                    request.getSubjectId());

            // check important to add to exam paper
            Set<ImportantView> set = new HashSet<>();
            for (Long importantId : request.getImportantIdList()) {
                Important important = checkEntityExistence(importantRepository.findById(importantId), "Important",
                        importantId);

                Important_Exam_Paper importantExamPaper = new Important_Exam_Paper(null, Exam_Status_Enum.DRAFT,
                        important, examPaper);

                importants.add(importantExamPaper);
                ImportantView view = ImportantMapper.INSTANCE.formImportantToView(important);
                set.add(view);
            }

            // update side in4
            examPaper.setExam(null);
            examPaper.setImportants(importants);
            examPaper.setSubject(subject);
            examPaper.setStatus(Exam_Status_Enum.DRAFT);
            examPaper.setIsUsed(false);
            examPaper.setDuration(request.getDuration());
            examPaper.setCreatedAt(Util.getCurrentDateTime());
            examPaper.setCreatedBy(Util.getAuthenticatedAccountId());

            examPaperRepository.save(examPaper);

            ExamPaperView examPaperView = ExamPaperMapper.INSTANCE.examPAperToView(examPaper);
            examPaperView.setImportants(set);

            return examPaperView;
        } catch (NotFoundException nfe) {
            throw nfe;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    @Override
    public ExamPaperFilePostmanResponseDTO getInfoFilePostman(Long examPaperId) {
        try {

            Exam_Paper examPaper = checkEntityExistence(
                    examPaperRepository.findById(examPaperId),
                    "Exam Paper",
                    examPaperId);

            String fileCollectionPostman = null;
            Long totalItem = 0L;
            if (examPaper.getFileCollectionPostman() != null) {
                fileCollectionPostman = new String(examPaper.getFileCollectionPostman(), StandardCharsets.UTF_8);

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(fileCollectionPostman);
                JsonNode items = root.get("item");
                if (items != null && items.isArray()) {
                    totalItem = (long) items.size();
                }
            }

            return new ExamPaperFilePostmanResponseDTO(fileCollectionPostman, examPaper.getIsComfirmFile(), totalItem,
                    examPaper.getLogRunPostman());
        } catch (NotFoundException e) {
            throw new RuntimeException("Exam Paper not found for ID: " + examPaperId, e);
        } catch (IOException e) {
            throw new RuntimeException("Error parsing Postman collection JSON", e);
        }
    }

    private void validateItemRequests(String fileContent) throws Exception {
        JSONObject collectionJson = new JSONObject(fileContent);
        JSONArray items = collectionJson.getJSONArray("item");

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            if (item.has("request")) {
                JSONObject request = item.getJSONObject("request");
                if (request.has("url")) {
                    JSONObject url = request.getJSONObject("url");

                    // Check protocol
                    if (!url.has("protocol") || !"http".equalsIgnoreCase(url.getString("protocol"))) {
                        throw new Exception("Invalid protocol for item: " + item.getString("name"));
                    }

                    // Check host
                    if (!url.has("host") || !url.getJSONArray("host").toList().contains("localhost")) {
                        throw new Exception("Invalid host for item: " + item.getString("name"));
                    }

                    // Check port
                    if (!url.has("port") || url.getString("port").isEmpty()) {
                        throw new Exception("Port is missing for item: " + item.getString("name"));
                    }
                } else {
                    throw new Exception("URL is missing in request for item: " + item.getString("name"));
                }
            } else {
                throw new Exception("Request is missing for item: " + item.getString("name"));
            }
        }
    }

    @Override
    public String confirmFilePostman(Long examPaperId) {

        Long authenticatedUserId = Util.getAuthenticatedAccountId();
        LocalDateTime time = Util.getCurrentDateTime();

        Exam_Paper examPaper;
        try {
            examPaper = examPaperRepository.findById(examPaperId)
                    .orElseThrow(() -> new NotFoundException("Exam Paper not found with id: " + examPaperId));
        } catch (NotFoundException e) {
            throw new IllegalArgumentException("Exam Paper with the provided ID does not exist.", e);
        }

        // Check Exam_Database and databaseScript
        Exam_Database examDatabase = examDatabaseRepository.findByExamPaper_ExamPaperId(examPaperId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Exam Database not found for the provided Exam Paper ID."));

        if (examDatabase.getDatabaseScript() == null || examDatabase.getDatabaseScript().isEmpty()) {
            throw new IllegalArgumentException("Exam Database Script cannot be null or empty.");
        }

        // Check fileCollectionPostman
        if (examPaper.getFileCollectionPostman() == null || examPaper.getFileCollectionPostman().length == 0) {
            throw new IllegalArgumentException("fileCollectionPostman is empty for this Exam Paper.");
        }

        // Convert byte[] to String
        String fileContent = new String(examPaper.getFileCollectionPostman(), StandardCharsets.UTF_8);

        // Call the validateItemRequests function to check the file
        try {
            validateItemRequests(fileContent);
        } catch (Exception e) {
            throw new IllegalArgumentException("Validation failed for fileCollectionPostman: " + e.getMessage(), e);
        }

        try {

            JsonNode fileCollectionJson = objectMapper.readTree(examPaper.getFileCollectionPostman());

            if (!fileCollectionJson.has("item") || !fileCollectionJson.get("item").isArray()) {
                throw new IllegalArgumentException("Invalid fileCollectionPostman format: 'item' array is required.");
            }

            List<String> fileItemNames = new ArrayList<>();
            fileCollectionJson.get("item").forEach(node -> {
                if (node.has("name")) {
                    fileItemNames.add(node.get("name").asText());
                }
            });

            List<Postman_For_Grading> gradingItems = postmanForGradingRepository
                    .findByExamPaper_ExamPaperIdAndStatusTrueOrderByOrderPriorityAsc(examPaperId);

            for (Postman_For_Grading gradingItem : gradingItems) {
                if (gradingItem.getExamQuestion() == null) {
                    throw new IllegalArgumentException(String.format(
                            "Grading item '%s' does not have a valid examQuestionId.",
                            gradingItem.getPostmanFunctionName()));
                }
                if (gradingItem.getTotalPmTest() == null || gradingItem.getTotalPmTest() <= 0) {
                    throw new IllegalArgumentException(String.format(
                            "Grading item '%s' has invalid totalPmTest: must be greater than 0.",
                            gradingItem.getPostmanFunctionName()));
                }

            }

            if (fileItemNames.size() != gradingItems.size()) {
                throw new IllegalArgumentException(
                        "Mismatch in number of items between fileCollectionPostman and Postman functions.");
            }

            for (int i = 0; i < gradingItems.size(); i++) {
                String expectedName = gradingItems.get(i).getPostmanFunctionName();
                String actualName = fileItemNames.get(i);

                if (!expectedName.equals(actualName)) {
                    throw new IllegalArgumentException(
                            String.format("Mismatch at index %d: expected '%s', found '%s'.", i, expectedName,
                                    actualName));
                }
            }

            for (int i = 0; i < gradingItems.size() - 1; i++) {
                Long currentOrderPriority = gradingItems.get(i).getOrderPriority();
                Long nextOrderPriority = gradingItems.get(i + 1).getOrderPriority();

                if (currentOrderPriority >= nextOrderPriority) {
                    throw new IllegalArgumentException(
                            String.format("orderPriority mismatch at index %d: '%d' should be less than '%d'.", i,
                                    currentOrderPriority, nextOrderPriority));
                }
            }

            float tolerance = 0.05f;

            Map<Long, Float> examQuestionScores = new HashMap<>();
            for (Postman_For_Grading gradingItem : gradingItems) {
                Exam_Question examQuestion = gradingItem.getExamQuestion();
                if (examQuestion != null) {
                    examQuestionScores.put(examQuestion.getExamQuestionId(),
                            examQuestionScores.getOrDefault(examQuestion.getExamQuestionId(), 0f)
                            + gradingItem.getScoreOfFunction());
                }
            }

            for (Postman_For_Grading gradingItem : gradingItems) {
                Exam_Question examQuestion = gradingItem.getExamQuestion();
                if (examQuestion != null) {
                    Float totalScoreForQuestion = examQuestionScores.get(examQuestion.getExamQuestionId());
                    if (totalScoreForQuestion == null
                            || Math.abs(totalScoreForQuestion - examQuestion.getExamQuestionScore()) > tolerance) {
                        throw new IllegalArgumentException(String.format(
                                "Score mismatch for Exam Question ID %d: expected %.2f, but found %.2f (within tolerance %.2f)",
                                examQuestion.getExamQuestionId(),
                                examQuestion.getExamQuestionScore(),
                                totalScoreForQuestion,
                                tolerance));
                    }
                }
            }

            examPaper.setIsComfirmFile(true);
            examPaperRepository.save(examPaper);

            saveLog(examPaper.getExamPaperId(), "Account [" + authenticatedUserId
                    + "] [Confirm all before grading successfully] at [" + time + "]");
            return "Successfully";

        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse fileCollectionPostman JSON.", e);
        }
    }

    @Override
    public void updateExamPaperToAnExam(ExamPaperToExamRequest request) throws NotFoundException, Exception {
        try {
            // check exam
            Exam exam = checkEntityExistence(examRepository.findById(request.getExamId()), "Exam", request.getExamId());
            // check exam paper
            Exam_Paper examPaper = checkEntityExistence(examPaperRepository.findById(request.getExamPaperId()),
                    "Exam Paper", request.getExamPaperId());

            if (exam.getType().equals(Exam_Type_Enum.EXAM)) {
                examPaper.setExam(exam);
                examPaper.setIsUsed(false);
                examPaperRepository.save(examPaper);
            } else {
                throw new NotFoundException("exam type is not EXAM");
            }
        } catch (NotFoundException | Exception e) {
            throw e;
        }
    }

    @Override
    public void updateIsused(Long examPaperId) throws NotFoundException, Exception {
        try {
            // check exam paper
            Exam_Paper examPaper = checkEntityExistence(examPaperRepository.findById(examPaperId), "Exam Paper",
                    examPaperId);
            // check exam
            Exam exam = checkEntityExistence(examRepository.findById(examPaper.getExam().getExamId()), "Exam",
                    examPaper.getExam().getExamId());
            if (exam.getType().equals(Exam_Type_Enum.EXAM)) {
                examPaper.setIsUsed(true);
                examPaperRepository.save(examPaper);
            } else {
                throw new NotFoundException("exam type is not EXAM");
            }
        } catch (NotFoundException | Exception e) {
            throw e;
        }
    }

}
