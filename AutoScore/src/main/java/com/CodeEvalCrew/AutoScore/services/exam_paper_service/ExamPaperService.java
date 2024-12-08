package com.CodeEvalCrew.AutoScore.services.exam_paper_service;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.mappers.ExamPaperMapper;
import com.CodeEvalCrew.AutoScore.mappers.ImportantMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamPaper.ExamPaperCreateRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamPaper.ExamPaperViewRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamPaperFilePostmanResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamPaperView;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.GherkinScenarioInfoDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ImportantView;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.NewmanResult;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PostmanFunctionInfo;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Exam_Status_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Question;
import com.CodeEvalCrew.AutoScore.models.Entity.Gherkin_Scenario;
import com.CodeEvalCrew.AutoScore.models.Entity.Important;
import com.CodeEvalCrew.AutoScore.models.Entity.Important_Exam_Paper;
import com.CodeEvalCrew.AutoScore.models.Entity.Postman_For_Grading;
import com.CodeEvalCrew.AutoScore.models.Entity.Subject;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamRepository;
import com.CodeEvalCrew.AutoScore.repositories.important_repository.ImportantExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.important_repository.ImportantRepository;
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
    private final ImportantExamPaperRepository importantExamPaperRepository;
    @Autowired
    private final ObjectMapper objectMapper;
    @Autowired
    private ISubjectRepository subjectRepository;

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
            checkEntityExistence(examRepository.findById(request.getExamId()), "Exam", request.getExamId());

            // crete spec
            Specification<Exam_Paper> spec = ExamPaperSpecification.hasForeignKey(request.getExamId(), "exam",
                    "examId");
            spec.and(ExamPaperSpecification.hasTrueStatus());

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

                Important_Exam_Paper importantExamPaper = new Important_Exam_Paper(null, Exam_Status_Enum.DRAFT,
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
            examPaper.setIsUsed(true);

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
    @Transactional
    public ExamPaperView updateExamPaper(Long id, ExamPaperCreateRequest request) throws NotFoundException {
        try {
            // check ExamPaper
            Exam_Paper examPaper = checkEntityExistence(examPaperRepository.findById(id), "Exam Paper", id);

            // check Exam
            Exam exam = checkEntityExistence(examRepository.findById(request.getExamId()), "Exam", request.getExamId());

            // update side in4
            examPaper.setExamPaperCode(request.getExamPaperCode());
            examPaper.setExam(exam);
            examPaper.setDuration(request.getDuration());
            examPaper.setStatus(Exam_Status_Enum.DRAFT);
            examPaper.setUpdatedAt(Util.getCurrentDateTime());
            examPaper.setUpdatedBy(Util.getAuthenticatedAccountId());

            examPaperRepository.save(examPaper);

            return ExamPaperMapper.INSTANCE.examPAperToView(examPaper);
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

    @Override
    public void importPostmanCollections(Long examPaperId, List<MultipartFile> files) throws Exception {
        try {
            Exam_Paper examPaper = examPaperRepository.findById(examPaperId)
                    .orElseThrow(() -> new NotFoundException("Exam Paper not found for ID: " + examPaperId));

            // Lấy danh sách các PostmanFunctionInfo từ examPaperId
            List<PostmanFunctionInfo> functionInfoList = getPostmanFunctionInfoByExamPaperId(examPaperId);

            List<String> allNewmanFunctionNames = new ArrayList<>(); // Lưu tất cả function từ Newman
            StringBuilder combinedNewmanLogs = new StringBuilder(); 

            for (MultipartFile file : files) {
                byte[] fileData = file.getBytes();

                String fileContent = new String(fileData, StandardCharsets.UTF_8);
                java.io.File tempFile = convertBytesToFile(fileData, file.getOriginalFilename());

                // Check if the file contains valid JSON
                if (!isValidJson(tempFile)) {
                    throw new Exception("File " + file.getOriginalFilename() + " contains invalid JSON.");
                }

                // Chạy Newman và xử lý kết quả
                String newmanOutput = runNewmanTest(tempFile);
                // combinedNewmanLogs.append("Output for file: ").append(file.getOriginalFilename()).append("\n")
                // .append(newmanOutput).append("\n"); // Ghi kết quả đầu ra
                   // Lưu log của Newman cho file hiện tại
            if (examPaper.getLogRunPostman() == null) {
                examPaper.setLogRunPostman(newmanOutput);
            } else {
                examPaper.setLogRunPostman(examPaper.getLogRunPostman() + "\n\n" + newmanOutput);
            }


                NewmanResult newmanResult = handleNewmanResult(newmanOutput, functionInfoList, examPaperId);

                // Ghi nhận tất cả functionNames từ Newman
                allNewmanFunctionNames.addAll(newmanResult.getFunctionNames());

                // Cập nhật fileCollectionPostman trong Postman_For_Grading

                updateFileCollectionPostmanForGrading(fileContent.getBytes(StandardCharsets.UTF_8), examPaperId);

                // Lưu thông tin vào examPaper bất kể kết quả Newman
                examPaper.setFileCollectionPostman(fileContent.getBytes(StandardCharsets.UTF_8));
                examPaper.setIsComfirmFile(false);
                // examPaper.setLogRunPostman(combinedNewmanLogs.toString());
                examPaperRepository.save(examPaper);

                // Dọn dẹp file tạm
                Files.deleteIfExists(tempFile.toPath());
            }
            List<String> functionNamesInDb = postmanForGradingRepository.findFunctionNamesByStatusTrue();
            List<String> functionNamesNotInNewman = functionNamesInDb.stream()
                    .filter(name -> !allNewmanFunctionNames.contains(name))
                    .collect(Collectors.toList());

            // Gọi hàm setStatusFalseForFunctionsNotInNewman
            setStatusFalseForFunctionsNotInNewman(functionNamesNotInNewman);
        } catch (NotFoundException e) {
            throw new Exception("Exam Paper with ID " + examPaperId + " not found.", e);
        } catch (Exception e) {
            throw new Exception("Failed to import files: " + e.getMessage(), e);
        }
    }

    private List<PostmanFunctionInfo> getPostmanFunctionInfoByExamPaperId(Long examPaperId) {
        return postmanForGradingRepository.findByExamPaper_ExamPaperIdAndStatusTrue(examPaperId)
                .stream()
                .map(postmanForGrading -> new PostmanFunctionInfo(
                        postmanForGrading.getPostmanFunctionName(),
                        postmanForGrading.getTotalPmTest()))
                .collect(Collectors.toList());
    }

    private void updateFileCollectionPostmanForGrading(byte[] fileData, Long examPaperId) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        // JsonNode collectionJson = objectMapper.readTree(fileData);
        JsonNode collectionJson = objectMapper.readTree(new String(fileData, StandardCharsets.UTF_8)); // Đảm bảo UTF-8

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
                updatedCollection.set("info", collectionJson.get("info")); // Copy thông tin từ info
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

        try {

            ProcessBuilder processBuilder = new ProcessBuilder(PathUtil.NEWMAN_CMD_PATH, "run",
                    "\"" + file.getAbsolutePath() + "\"");
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            // Đọc kết quả từ luồng đầu ra của Newman
            // try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                 
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Failed to run Newman test: " + e.getMessage(), e);
        }

        return output.toString(); // Trả về chuỗi kết quả đầu ra
    }

    private NewmanResult handleNewmanResult(String newmanOutput, List<PostmanFunctionInfo> expectedFunctionInfo,
            Long examPaperId)
            throws NotFoundException {
        // Phân tích đầu ra của Newman và tạo kết quả
        NewmanResult result = parseNewmanOutput(newmanOutput, expectedFunctionInfo);

        // Lấy danh sách functionNames từ kết quả Newman
        List<String> functionNamesFromNewman = result.getFunctionNames();

        List<String> functionNamesInDb = postmanForGradingRepository.findFunctionNamesByStatusTrue();

        // Duyệt qua các functionNames từ kết quả Newman
        for (int i = 0; i < functionNamesFromNewman.size(); i++) {
            String functionName = functionNamesFromNewman.get(i);
            Long newTotalPmTest = (long) result.getTotalPmTests().get(i); // Chuyển đổi sang Long

            // Tìm functionName trong expectedFunctionInfo (database)
            Optional<PostmanFunctionInfo> expectedInfoOpt = expectedFunctionInfo.stream()
                    .filter(info -> info.getFunctionName().equals(functionName))
                    .findFirst();

            if (expectedInfoOpt.isPresent()) {
                PostmanFunctionInfo expectedInfo = expectedInfoOpt.get();

                // Nếu totalPmTests khác, cập nhật database
                if (!expectedInfo.getTotalPmTest().equals(newTotalPmTest)) {
                    updateTotalPmTestInDatabase(functionName, newTotalPmTest);
                }

            } else {
                // Nếu không tìm thấy functionName trong database, tạo mới Postman_For_Grading
                createNewPostmanForGrading(functionName, newTotalPmTest, examPaperId);
                // System.out.println("Created new function in database: " + functionName);
            }
        }

        return result;
    }

    private void setStatusFalseForFunctionsNotInNewman(List<String> functionNamesNotInNewman) {
        if (!functionNamesNotInNewman.isEmpty()) {
            // Chỉ lấy những Postman_For_Grading có status = true
            List<Postman_For_Grading> postmenToUpdate = postmanForGradingRepository
                    .findByPostmanFunctionNameInAndStatusTrue(functionNamesNotInNewman);
            for (Postman_For_Grading postman : postmenToUpdate) {
                postman.setStatus(false);
                postman.setPostmanFunctionName(null);
                // Kiểm tra nếu gherkinScenario có dữ liệu thì đặt về null
                if (postman.getGherkinScenario() != null) {
                    postman.setGherkinScenario(null);
                }
            }
            postmanForGradingRepository.saveAll(postmenToUpdate);
        }
    }

    private void createNewPostmanForGrading(String functionName, Long totalPmTest, Long examPaperId)
            throws NotFoundException {
        // Lấy Exam_Paper từ database
        Exam_Paper examPaper = examPaperRepository.findById(examPaperId)
                .orElseThrow(() -> new NotFoundException("Exam Paper not found for ID: " + examPaperId));

        // Tạo mới Postman_For_Grading
        Postman_For_Grading newPostmanForGrading = new Postman_For_Grading();
        newPostmanForGrading.setPostmanFunctionName(functionName);
        newPostmanForGrading.setTotalPmTest(totalPmTest);
        newPostmanForGrading.setExamPaper(examPaper);
        newPostmanForGrading.setStatus(true);

        // Lưu vào database
        postmanForGradingRepository.save(newPostmanForGrading);
    }

    private void updateTotalPmTestInDatabase(String functionName, Long newTotalPmTest) throws NotFoundException {
        Postman_For_Grading postmanForGrading = postmanForGradingRepository
                .findByPostmanFunctionName(functionName)
                .orElseThrow(() -> new NotFoundException("Postman Function Name not found: " + functionName));

        postmanForGrading.setTotalPmTest(newTotalPmTest);
        postmanForGradingRepository.save(postmanForGrading);

        System.out.println("Updated " + functionName + " in database to " + newTotalPmTest);
    }

    private NewmanResult parseNewmanOutput(String newmanOutput, List<PostmanFunctionInfo> expectedFunctionInfo) {
        NewmanResult result = new NewmanResult();
        List<String> functionNames = new ArrayList<>();
        List<Integer> totalPmTests = new ArrayList<>();
        String[] lines = newmanOutput.split("\n");
        String currentFunctionName = null;
        int currentFunctionTestCount = 0;

        boolean isParsingFunctions = true; // Cờ để dừng khi gặp bảng

        for (String line : lines) {
            // Nếu gặp bảng, dừng phân tích
            if (line.trim().startsWith("┌") || line.trim().startsWith("│") || line.trim().startsWith("└")) {
                isParsingFunctions = false;
            }

            if (!isParsingFunctions) {
                break; // Ngừng xử lý nếu bảng đã xuất hiện
            }

            // Nếu dòng bắt đầu bằng "→", đó là tên function mới
            if (line.startsWith("→")) {
                if (currentFunctionName != null) { // Lưu function trước đó nếu có
                    functionNames.add(currentFunctionName);
                    totalPmTests.add(currentFunctionTestCount);
                }
                currentFunctionName = line.substring(2).trim(); // Bỏ "→" và lấy tên function
                currentFunctionTestCount = 0; // Reset bộ đếm
            }

            // Nếu dòng là test (bắt đầu bằng số hoặc dấu "√"), tăng bộ đếm
            if (line.trim().matches("^\\d+.*") || line.trim().startsWith("√")) {
                currentFunctionTestCount++;
            }
        }

        // Thêm function cuối cùng vào danh sách kết quả nếu còn function chưa lưu
        if (currentFunctionName != null) {
            functionNames.add(currentFunctionName);
            totalPmTests.add(currentFunctionTestCount);
        }

        result.setFunctionNames(functionNames);
        result.setTotalPmTests(totalPmTests);
        return result;
    }

    @Override
    public List<Long> getExamQuestionIdsByExamPaperId(Long examPaperId) throws NotFoundException {
        Exam_Paper examPaper = checkEntityExistence(examPaperRepository.findById(examPaperId), "Exam Paper",
                examPaperId);

        // Extract question IDs from the associated Exam_Question set
        List<Long> questionIds = examPaper.getExamQuestions().stream()
                .map(Exam_Question::getExamQuestionId)
                .collect(Collectors.toList());

        return questionIds;
    }

    @Override
    public List<GherkinScenarioInfoDTO> getGherkinScenariosByExamPaperId(Long examPaperId) throws NotFoundException {
        Exam_Paper examPaper = examPaperRepository.findById(examPaperId)
                .orElseThrow(() -> new NotFoundException("Exam Paper không tồn tại"));

        List<GherkinScenarioInfoDTO> result = new ArrayList<>();
        examPaper.getExamQuestions().forEach(examQuestion -> {
            Set<Gherkin_Scenario> gherkinScenarios = examQuestion.getGherkinScenarios();
            gherkinScenarios.forEach(gherkinScenario -> {
                result.add(new GherkinScenarioInfoDTO(
                        examPaperId,
                        examQuestion.getExamQuestionId(),
                        gherkinScenario.getGherkinScenarioId()));
            });
        });

        return result;
    }

    @Override
    public byte[] exportPostmanCollection(Long examPaperId) throws Exception {
        // Lấy Exam_Paper từ ID
        Exam_Paper examPaper = examPaperRepository.findById(examPaperId)
                .orElseThrow(() -> new Exception("Exam Paper not found with ID: " + examPaperId));

        // Trả về nội dung fileCollectionPostman dưới dạng byte[]
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
        } catch (Exception e) {
            System.out.println(e.getCause());
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
            // Tìm Exam_Paper bằng ID
            Exam_Paper examPaper = checkEntityExistence(
                    examPaperRepository.findById(examPaperId),
                    "Exam Paper",
                    examPaperId);

            // Chuyển fileCollectionPostman từ byte[] sang chuỗi JSON UTF-8
            String fileCollectionPostman = null;
            Long totalItem = 0L; // Initialize total item counter
            if (examPaper.getFileCollectionPostman() != null) {
                fileCollectionPostman = new String(examPaper.getFileCollectionPostman(), StandardCharsets.UTF_8);

                // Parse JSON and count items
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(fileCollectionPostman);
                JsonNode items = root.get("item");
                if (items != null && items.isArray()) {
                    totalItem = (long) items.size(); // Count number of items
                }
            }

            // Trả về DTO
            return new ExamPaperFilePostmanResponseDTO(fileCollectionPostman, examPaper.getIsComfirmFile(), totalItem,  examPaper.getLogRunPostman() );
        } catch (NotFoundException e) {
            throw new RuntimeException("Exam Paper not found for ID: " + examPaperId, e);
        } catch (IOException e) {
            throw new RuntimeException("Error parsing Postman collection JSON", e);
        }
    }

    @Override
    public String confirmFilePostman(Long examPaperId) {
        // Lấy Exam_Paper từ examPaperId
        Exam_Paper examPaper;
        try {
            examPaper = examPaperRepository.findById(examPaperId)
                    .orElseThrow(() -> new NotFoundException("Exam Paper not found with id: " + examPaperId));
        } catch (NotFoundException e) {
            throw new IllegalArgumentException("Exam Paper with the provided ID does not exist.", e);
        }

        // Kiểm tra fileCollectionPostman có tồn tại và không rỗng
        if (examPaper.getFileCollectionPostman() == null || examPaper.getFileCollectionPostman().length == 0) {
            throw new IllegalArgumentException("fileCollectionPostman is empty for this Exam Paper.");
        }

        try {
            // Chuyển đổi fileCollectionPostman từ byte[] thành JsonNode
            JsonNode fileCollectionJson = objectMapper.readTree(examPaper.getFileCollectionPostman());

            // Kiểm tra cấu trúc "item" có tồn tại và là mảng
            if (!fileCollectionJson.has("item") || !fileCollectionJson.get("item").isArray()) {
                throw new IllegalArgumentException("Invalid fileCollectionPostman format: 'item' array is required.");
            }

            // Lấy danh sách tên từ "item" trong fileCollectionPostman
            List<String> fileItemNames = new ArrayList<>();
            fileCollectionJson.get("item").forEach(node -> {
                if (node.has("name")) {
                    fileItemNames.add(node.get("name").asText());
                }
            });

            // Lấy danh sách Postman_For_Grading theo Exam_Paper, sắp xếp theo orderPriority
            List<Postman_For_Grading> gradingItems = postmanForGradingRepository
                    .findByExamPaper_ExamPaperIdAndStatusTrueOrderByOrderPriority(examPaperId);

            // Kiểm tra kích thước danh sách
            if (fileItemNames.size() != gradingItems.size()) {
                throw new IllegalArgumentException(
                        "Mismatch in number of items between fileCollectionPostman and Postman functions.");
            }

            // So sánh từng phần tử theo thứ tự
            for (int i = 0; i < gradingItems.size(); i++) {
                String expectedName = gradingItems.get(i).getPostmanFunctionName();
                String actualName = fileItemNames.get(i);

                if (!expectedName.equals(actualName)) {
                    throw new IllegalArgumentException(
                            String.format("Mismatch at index %d: expected '%s', found '%s'.", i, expectedName,
                                    actualName));
                }
            }

            // Nếu tất cả khớp, cập nhật trạng thái isConfirmFile của Exam_Paper
            examPaper.setIsComfirmFile(true);
            examPaperRepository.save(examPaper);

            // Trả về thành công
            return "Successfully";

        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse fileCollectionPostman JSON.", e);
        }
    }

}
