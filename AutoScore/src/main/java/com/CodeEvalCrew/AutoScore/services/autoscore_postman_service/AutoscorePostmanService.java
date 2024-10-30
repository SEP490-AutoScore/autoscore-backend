package com.CodeEvalCrew.AutoScore.services.autoscore_postman_service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.mappers.SourceDetailMapperforAutoscore;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.StudentDeployResult;
import com.CodeEvalCrew.AutoScore.models.DTO.StudentSourceInfoDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Database;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Question;
import com.CodeEvalCrew.AutoScore.models.Entity.Postman_For_Grading;
import com.CodeEvalCrew.AutoScore.models.Entity.Score;
import com.CodeEvalCrew.AutoScore.models.Entity.Score_Detail;
import com.CodeEvalCrew.AutoScore.models.Entity.Source_Detail;
import com.CodeEvalCrew.AutoScore.models.Entity.Student;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamQuestionRepository;
import com.CodeEvalCrew.AutoScore.repositories.examdatabase_repository.IExamDatabaseRepository;
import com.CodeEvalCrew.AutoScore.repositories.postman_for_grading.PostmanForGradingRepository;
import com.CodeEvalCrew.AutoScore.repositories.score_detail_repository.ScoreDetailRepository;
import com.CodeEvalCrew.AutoScore.repositories.score_repository.ScoreRepository;
import com.CodeEvalCrew.AutoScore.repositories.source_repository.SourceDetailRepository;
import com.CodeEvalCrew.AutoScore.repositories.source_repository.SourceRepository;
import com.CodeEvalCrew.AutoScore.repositories.student_repository.StudentRepository;
import com.CodeEvalCrew.AutoScore.services.autoscore_postman_service.Utils.AutoscoreInitUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.okhttp.OkHttpDockerCmdExecFactory;

@Service
public class AutoscorePostmanService implements IAutoscorePostmanService {

    private static final String DB_URL = "jdbc:sqlserver://ADMIN-PC\\SQLEXPRESS;databaseName=master;user=sa;password=1234567890;encrypt=false;trustServerCertificate=true;";
    private static final String DB_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    @Autowired
    private SourceRepository sourceRepository;
    @Autowired
    private SourceDetailRepository sourceDetailRepository;
    @Autowired
    private SourceDetailMapperforAutoscore sourceDetailMapper;
    @Autowired
    private IExamDatabaseRepository examDatabaseRepository;
    @Autowired
    private IExamPaperRepository examPaperRepository;
    @Autowired
    private ScoreRepository scoreRepository;
    @Autowired
    private ScoreDetailRepository scoreDetailRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private IExamQuestionRepository examQuestionRepository;
    @Autowired
    private PostmanForGradingRepository postmanForGradingRepository;

    String directoryPath = "D:/Desktop/all collection postman";

    @Override
    public List<StudentSourceInfoDTO> gradingFunction(Long examPaperId, int numberDeploy) {
        List<StudentSourceInfoDTO> studentSources = sourceDetailRepository
                .findBySource_ExamPaper_ExamPaperIdOrderByStudent_StudentId(examPaperId)
                .stream()
                .map(sourceDetail -> sourceDetailMapper.toDTO(sourceDetail))
                .collect(Collectors.toList());

        deleteAndCreateDatabaseByExamPaperId(examPaperId);
        AutoscoreInitUtils.deleteAllFilesAndFolders(directoryPath);

        processStudentSolutions(studentSources, examPaperId);

        return studentSources;
    }

    public void processStudentSolutions(List<StudentSourceInfoDTO> studentSources, Long examPaperId) {
        int batchSize = 2;
        int totalBatches = (int) Math.ceil((double) studentSources.size() / batchSize);

        for (int batchIndex = 0; batchIndex < totalBatches; batchIndex++) {
            int start = batchIndex * batchSize;
            int end = Math.min(start + batchSize, studentSources.size());
            List<StudentSourceInfoDTO> currentBatch = studentSources.subList(start, end);

            ExecutorService executor = Executors.newFixedThreadPool(currentBatch.size());
            Map<Future<StudentDeployResult>, StudentSourceInfoDTO> futureToStudentSourceMap = new HashMap<>();
            List<StudentSourceInfoDTO> successfulDeployments = new ArrayList<>(); // List to track successful
                                                                                  // deployments

            for (int i = 0; i < currentBatch.size(); i++) {
                StudentSourceInfoDTO studentSource = currentBatch.get(i);
                Path dirPath = Paths.get(studentSource.getStudentSourceCodePath());
                int port = AutoscoreInitUtils.BASE_PORT + i;
                Long studentId = studentSource.getStudentId();

                createFileCollectionPostman(examPaperId, studentSource.getSourceDetailId(), port);

                // Xóa container docker
                try {
                    deleteContainerAndImages();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Failed to delete containers and images: " + e.getMessage());
                }

                Future<StudentDeployResult> future = executor.submit(() -> {

                    try {
                        AutoscoreInitUtils.removeDockerFiles(dirPath);
                        var csprojAndVersion = AutoscoreInitUtils.findCsprojAndDotnetVersion(dirPath);

                        if (csprojAndVersion != null) {
                            AutoscoreInitUtils.createDockerfile(dirPath, csprojAndVersion.getKey(),
                                    csprojAndVersion.getValue(), port);
                            AutoscoreInitUtils.createDockerCompose(dirPath, studentId, port);
                            findAndUpdateAppsettings(dirPath, examPaperId, port);

                        }

                        return deployStudentSolution(studentSource);

                    } catch (IOException e) {
                        e.printStackTrace();
                        return new StudentDeployResult(studentId, false, "IOException: " + e.getMessage());
                    }
                });

                futureToStudentSourceMap.put(future, studentSource);
            }
            executor.shutdown();

            try {
                executor.awaitTermination(60, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Hiển thị kết quả triển khai cho mỗi sinh viên trong nhóm hiện tại
            for (Map.Entry<Future<StudentDeployResult>, StudentSourceInfoDTO> entry : futureToStudentSourceMap
                    .entrySet()) {
                Future<StudentDeployResult> future = entry.getKey();
                StudentSourceInfoDTO studentSource = entry.getValue();

                try {
                    StudentDeployResult result = future.get();
                    System.out.println(result.getMessage() + " for studentId: " + result.getStudentId());

                    if (!result.isSuccessful()) {
                        recordFailure(result.getStudentId(), examPaperId, "Cannot deploy Docker.");
                    } else {
                        successfulDeployments.add(studentSource);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            // Run Newman cho các triển khai thành công trong nhóm hiện tại
            if (!successfulDeployments.isEmpty()) {
                for (StudentSourceInfoDTO successfulStudent : successfulDeployments) {

                    Map<String, Integer> passedFunctionNames = getAndRunPostmanCollection(
                            successfulStudent.getStudentId(), successfulStudent.getSourceDetailId());

                    // Convert List to Map to get count of each function's success
                    Map<String, Long> functionPassedCountMap = passedFunctionNames.entrySet().stream()
                            .collect(Collectors.groupingBy(Map.Entry::getKey,
                                    Collectors.summingLong(Map.Entry::getValue)));

                    System.out.println(
                            "Function passed count map for studentId " + successfulStudent.getStudentId() + ": "
                                    + functionPassedCountMap);

                    // Call saveScoreAndScoreDetail with the Map
                    saveScoreAndScoreDetail(successfulStudent.getStudentId(), examPaperId, functionPassedCountMap);
                    deleteAndCreateDatabaseByExamPaperId(examPaperId);
                }
            } else {
                System.out.println("No successful deployments found to run Newman.");
            }
        }
    }

    private Map<String, Integer> getAndRunPostmanCollection(Long studentId, Long sourceDetailId) {
        Map<String, Integer> functionResults = new HashMap<>();
        String currentFunction = null;
        int passCount = 0;
        boolean hasError = false; // Cờ để kiểm tra xem hàm hiện tại có lỗi hay không

        try {
            // Tạo thư mục sinh viên nếu chưa có
            Path studentDir = Paths.get("D:/Desktop/all collection postman", String.valueOf(studentId));
            Files.createDirectories(studentDir);

            // Lấy dữ liệu collection và tạo file Postman
            Source_Detail sourceDetail = sourceDetailRepository.findById(sourceDetailId)
                    .orElseThrow(() -> new RuntimeException("Source_Detail not found with ID: " + sourceDetailId));

            Path postmanFilePath = studentDir.resolve(studentId + ".json");

            // Đảm bảo tải `fileCollectionPostman`
            Hibernate.initialize(sourceDetail.getFileCollectionPostman());
            byte[] postmanCollection = sourceDetail.getFileCollectionPostman();

            try {
                // Tạm dừng 3 giây (3000 milliseconds)
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Debug: fileCollectionPostman for sourceDetailId " + sourceDetailId + " is "
                    + (postmanCollection == null ? "null" : "not null"));
            Objects.requireNonNull(postmanCollection,
                    "fileCollectionPostman is null for sourceDetailId: " + sourceDetailId);
            Files.write(postmanFilePath, postmanCollection);

            // Chờ cho file được tạo thành công trước khi tiếp tục
            int waitTimeInSeconds = 10; // Thời gian chờ tối đa (giây)
            int intervalInMilliseconds = 500; // Khoảng thời gian giữa các lần kiểm tra (ms)
            int waited = 0;
            while (!Files.exists(postmanFilePath) && waited < waitTimeInSeconds * 1000) {
                Thread.sleep(intervalInMilliseconds);
                waited += intervalInMilliseconds;
            }

            if (!Files.exists(postmanFilePath)) {
                throw new IOException("Failed to create Postman collection file within timeout.");
            }

            // Đường dẫn tới newman
            String newmanPath = "C:/Users/Admin/AppData/Roaming/npm/newman.cmd";
            System.out.println("Running Newman for studentId: " + studentId);

            // Cấu hình ProcessBuilder
            ProcessBuilder processBuilder = new ProcessBuilder(newmanPath, "run", postmanFilePath.toString());
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Đặt tên file lưu output
            Path outputFile = studentDir.resolve(studentId + ".txt");

            try (
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream(), "UTF-8"));
                    BufferedWriter writer = Files.newBufferedWriter(outputFile)) {

                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line); // Ghi dòng đầu ra vào file
                    writer.newLine();

                    line = line.trim();

                    if (line.startsWith("→")) {
                        // Nếu có hàm trước đó và không bị lỗi, lưu kết quả
                        if (currentFunction != null && !hasError) {
                            functionResults.put(currentFunction, passCount);
                        }
                        // Khởi tạo lại biến cho hàm mới
                        currentFunction = line.split("\\s+")[1];
                        passCount = 0;
                        hasError = false; // Reset lỗi cho hàm mới
                    } else if (line.matches(".*\\[(4\\d{2}|5\\d{2}) .+\\].*") || line.contains("[errored]")) {
                        // Đánh dấu hàm hiện tại là lỗi
                        hasError = true;
                    } else if (line.startsWith("√") && !hasError) {
                        // Chỉ tăng passCount nếu không có lỗi
                        passCount++;
                    }
                }

                // Lưu kết quả của hàm cuối cùng nếu có và không bị lỗi
                if (currentFunction != null && !hasError) {
                    functionResults.put(currentFunction, passCount);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Newman executed successfully for studentId: " + studentId);
            } else {
                // System.err.println(
                //         "Newman execution failed with exit code: " + exitCode + " for studentId: " + studentId);
                System.err.println(
                    "Newman execution failed with exit code: " + exitCode + " for studentId: " + studentId +
                    ". Please check " + outputFile.toString() + " for detailed error logs."
                );
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error running Newman or storing Postman collection for studentId " + studentId + ": "
                    + e.getMessage());
        }

        // In kết quả cuối cùng
        functionResults.forEach((function, count) -> {
            System.out.println("noPmtestAchieve for " + function + ": " + count);
        });

        return functionResults;
    }

    private Float calculateScoreAchieve(Postman_For_Grading postmanFunction, Long noPmtestAchieve,
            Map<String, Long> functionPassedCountMap, Map<Long, Float> parentScoreMap) {
        Long totalPmtest = postmanFunction.getTotalPmTest();
        Float scoreOfFunction = postmanFunction.getScoreOfFunction();
        Long parentId = postmanFunction.getPostmanForGradingParentId();

        // Xác định đây là một chức năng cha nếu parentId == null hoặc là tự tham chiếu
        // (trỏ tới chính nó)
        if (parentId == null || parentId.equals(postmanFunction.getPostmanForGradingId())) {
            Float scoreAchieve = (noPmtestAchieve / (float) totalPmtest) * scoreOfFunction;
            System.out.println("Calculating score for parent function: " + postmanFunction.getPostmanFunctionName() +
                    ", noPmtestAchieve: " + noPmtestAchieve +
                    ", totalPmtest: " + totalPmtest +
                    ", scoreAchieve: " + scoreAchieve);
            return scoreAchieve;
        }
        // Nếu là chức năng con và chức năng cha có scoreAchieve = 0
        else if (parentScoreMap.getOrDefault(parentId, 0.0f) == 0.0f) {
            Postman_For_Grading parentFunction = postmanForGradingRepository.findById(parentId).orElse(null);
            String parentFunctionName = (parentFunction != null) ? parentFunction.getPostmanFunctionName() : "Unknown";

            System.out.println("Parent function '" + parentFunctionName + "' has scoreAchieve = 0, so child function '"
                    + postmanFunction.getPostmanFunctionName() + "' will also have scoreAchieve = 0");
            return 0.0f;
        }
        // Trường hợp khác
        Float scoreAchieve = (noPmtestAchieve / (float) totalPmtest) * scoreOfFunction;
        System.out.println("Calculating score for child function: " + postmanFunction.getPostmanFunctionName() +
                ", noPmtestAchieve: " + noPmtestAchieve +
                ", totalPmtest: " + totalPmtest +
                ", scoreAchieve: " + scoreAchieve);
        return scoreAchieve;
    }

    public void saveScoreAndScoreDetail(Long studentId, Long examPaperId,
            Map<String, Long> functionPassedCountMap) {
        Student student = studentRepository.findById(studentId).orElse(null);
        Exam_Paper examPaper = examPaperRepository.findById(examPaperId).orElse(null);

        if (student == null || examPaper == null) {
            System.err.println("Student hoặc Exam Paper không tồn tại.");
            return;
        }

        Score score = new Score();
        score.setStudent(student);
        score.setExamPaper(examPaper);
        score.setGradedAt(LocalDateTime.now());
        // score.setFlag(true);

        // Lưu tạm Score để có thể dùng làm khóa ngoại cho Score_Detail
        scoreRepository.save(score);

        Map<Long, Float> parentScoreMap = new HashMap<>(); // Lưu điểm của chức năng cha
        float totalScoreAchieve = 0f; // Biến để lưu tổng điểm của tất cả các Score_Detail

        for (Exam_Question question : examPaper.getExamQuestions()) {
            for (Postman_For_Grading postmanFunction : question.getPostmanForGradingEntries()) {
                Score_Detail scoreDetail = new Score_Detail();
                scoreDetail.setScore(score);
                scoreDetail.setExamQuestion(question);
                scoreDetail.setPostmanFunctionName(postmanFunction.getPostmanFunctionName());
                scoreDetail.setScoreOfFunction(postmanFunction.getScoreOfFunction());
                scoreDetail.setTotalPmtest(postmanFunction.getTotalPmTest());

                Long noPmtestAchieve = functionPassedCountMap.getOrDefault(postmanFunction.getPostmanFunctionName(),
                        0L);
                System.out.println("noPmtestAchieve for function " + postmanFunction.getPostmanFunctionName() + ": " +
                        noPmtestAchieve);

                scoreDetail.setNoPmtestAchieve(noPmtestAchieve);

                // Tính scoreAchieve
                Float scoreAchieve = calculateScoreAchieve(postmanFunction, noPmtestAchieve,
                        functionPassedCountMap, parentScoreMap);
                scoreDetail.setScoreAchieve(scoreAchieve);

                // Cộng dồn scoreAchieve vào totalScoreAchieve
                totalScoreAchieve += scoreAchieve;

                // Nếu là chức năng cha, cập nhật parentScoreMap với scoreAchieve
                if (postmanFunction.getPostmanForGradingParentId() == null ||
                        postmanFunction.getPostmanForGradingParentId()
                                .equals(postmanFunction.getPostmanForGradingId())) {
                    parentScoreMap.put(postmanFunction.getPostmanForGradingId(), scoreAchieve);
                    System.out
                            .println("Updated parentScoreMap for function " + postmanFunction.getPostmanFunctionName() +
                                    " with scoreAchieve: " + scoreAchieve);
                }

                // Lưu scoreDetail vào database
                scoreDetailRepository.save(scoreDetail);
                System.out.println("Saved score detail for function " + postmanFunction.getPostmanFunctionName() +
                        ", scoreAchieve: " + scoreAchieve);
            }
        }

        // Cập nhật lại tổng điểm vào Score
        score.setTotalScore(totalScoreAchieve);
        scoreRepository.save(score); // Lưu lại Score với totalScore đã cập nhật
        System.out.println("Saved total score: " + totalScoreAchieve);
    }

    private StudentDeployResult deployStudentSolution(StudentSourceInfoDTO studentSource) {
        Path dirPath = Paths.get(studentSource.getStudentSourceCodePath());
        Long studentId = studentSource.getStudentId(); // Lưu lại studentId để trả về kết quả

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("docker-compose", "up", "-d", "--build");
            processBuilder.directory(dirPath.toFile());
            processBuilder.inheritIO();

            Process process = processBuilder.start();

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return new StudentDeployResult(studentId, true, "Deploy thành công");
            } else {
                return new StudentDeployResult(studentId, false, "Deploy thất bại với mã thoát: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new StudentDeployResult(studentId, false, "Exception: " + e.getMessage());
        }
    }

    private void recordFailure(Long studentId, Long examPaperId, String reason) {
        Student student = scoreRepository.findStudentById(studentId);
        if (student != null && student.getOrganization() != null) {
            Score score = new Score();
            score.setStudent(student);

            Exam_Paper examPaper = new Exam_Paper();
            examPaper.setExamPaperId(examPaperId);
            score.setExamPaper(examPaper);
            score.setTotalScore(0.0f);
            score.setGradedAt(LocalDateTime.now());
            score.setReason(reason);
            // score.setFlag(false);
            // score.setOrganization(student.getOrganization());

            scoreRepository.save(score);
        } else {
            System.err.println("Student or Organization not found for studentId: " + studentId);
        }
    }

    public void updateAppsettingsJson(Path filePath, Long examPaperId, int port) throws IOException {
        String content = Files.readString(filePath, StandardCharsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootNode = (ObjectNode) objectMapper.readTree(content);
        String databaseName = examDatabaseRepository.findDatabaseNameByExamPaperId(examPaperId);
        if (rootNode.has("ConnectionStrings")) {
            ObjectNode connectionStringsNode = (ObjectNode) rootNode.get("ConnectionStrings");
            connectionStringsNode.fieldNames().forEachRemaining(key -> {
                connectionStringsNode.put(key, String.join(";",
                        "Server=192.168.2.16\\SQLEXPRESS",
                        "uid=sa",
                        "pwd=1234567890",
                        "database=" + databaseName,
                        "TrustServerCertificate=True"));
            });
        }
        content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);

        String portPattern = "\"Url\"\\s*:\\s*\"http://\\*:[0-9]+\"";
        String replacement = "\"Url\": \"http://*:" + port + "\"";
        content = content.replaceAll(portPattern, replacement);
        Files.writeString(filePath, content, StandardCharsets.UTF_8);
    }

    public void findAndUpdateAppsettings(Path dirPath, Long examPaperId, int port) throws IOException {
        try (Stream<Path> folders = Files.walk(dirPath, 1)) {
            List<Path> targetDirs = folders
                    .filter(Files::isDirectory)
                    .filter(path -> {
                        try (Stream<Path> files = Files.walk(path, 1)) {
                            return files.anyMatch(file -> file.getFileName().toString().equalsIgnoreCase("Program.cs"));
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
            for (Path targetDir : targetDirs) {
                try (Stream<Path> paths = Files.walk(targetDir)) {
                    paths.filter(Files::isRegularFile)
                            .filter(path -> path.toString().endsWith("appsettings.json"))
                            .forEach(path -> {
                                try {
                                    updateAppsettingsJson(path, examPaperId, port);
                                } catch (IOException e) {
                                    System.err.println("Error updating: " + path + " - " + e.getMessage());
                                }
                            });
                }
            }
        }
    }

    private void deleteAndCreateDatabaseByExamPaperId(Long examPaperId) {
        try {
            Class.forName(DB_DRIVER);
            try (Connection connection = DriverManager.getConnection(DB_URL);
                    Statement statement = connection.createStatement()) {

                String databaseName = examDatabaseRepository.findDatabaseNameByExamPaperId(examPaperId);
                Exam_Database examDatabase = examDatabaseRepository.findByExamPaperExamPaperId(examPaperId);

                if (examDatabase != null) {
                    Long examDatabaseId = examDatabase.getExamDatabaseId();
                    System.out.println("Found Exam_Database ID: " + examDatabaseId);

                    if (databaseName != null && !databaseName.isEmpty()) {
                        String sql = "IF EXISTS (SELECT name FROM sys.databases WHERE name = '" + databaseName + "') " +
                                "BEGIN " +
                                "   ALTER DATABASE [" + databaseName + "] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; " +
                                "   DROP DATABASE [" + databaseName + "]; " +
                                "END";
                        statement.executeUpdate(sql);
                        System.out.println("Database " + databaseName + " has been deleted.");
                    }

                    if (examDatabase.getDatabaseFile() != null) {
                        String createDatabaseSQL = new String(examDatabase.getDatabaseFile());

                        String[] sqlCommands = createDatabaseSQL.split("(?i)\\bGO\\b");

                        for (String sqlCommand : sqlCommands) {
                            if (!sqlCommand.trim().isEmpty()) {
                                statement.executeUpdate(sqlCommand.trim());
                            }
                        }
                        System.out.println("Database " + databaseName + " has been created.");
                    } else {
                        System.out.println("No database file found for examPaperId: " + examPaperId);
                    }
                } else {
                    System.out.println("No Exam_Database found for examPaperId: " + examPaperId);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to delete and create database for examPaperId: " + examPaperId);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("SQL Server JDBC Driver not found.");
        }
    }

    public void deleteContainerAndImages() throws IOException { 
        DockerClient dockerClient = DockerClientBuilder.getInstance("tcp://localhost:2375")
                .withDockerCmdExecFactory(new OkHttpDockerCmdExecFactory())
                .build();
    
        try {
            // Remove all containers
            List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();
            for (Container container : containers) {
                System.out.println("Removing container " + container.getNames()[0] + " (" + container.getId().substring(0, 12) + ")");
                dockerClient.removeContainerCmd(container.getId()).withForce(true).exec();
            }
            System.out.println("All containers have been removed.");
    
            // Remove all images
            List<Image> images = dockerClient.listImagesCmd().withDanglingFilter(false).exec();
            for (Image image : images) {
                System.out.println("Removing image " + image.getId().substring(0, 12));
                dockerClient.removeImageCmd(image.getId()).withForce(true).exec();
            }
            System.out.println("All images have been removed.");
    
            // Remove unused networks
            List<Network> networks = dockerClient.listNetworksCmd().exec();
            for (Network network : networks) {
                // Ignore default Docker networks
                if (!network.getName().equals("bridge") && !network.getName().equals("host") && !network.getName().equals("none")) {
                    System.out.println("Removing network " + network.getName() + " (" + network.getId().substring(0, 12) + ")");
                    dockerClient.removeNetworkCmd(network.getId()).exec();
                }
            }
            System.out.println("All unused networks have been removed.");
    
        } catch (DockerException e) {
            e.printStackTrace();
            throw new RuntimeException("Docker operation failed: " + e.getMessage());
        } finally {
            dockerClient.close(); // Close Docker client
        }
    }

    // @Transactional
    public void createFileCollectionPostman(Long examPaperId, Long sourceDetailId, int port) {
        // Fetch the exam paper's Postman file collection
        Exam_Paper examPaper = examPaperRepository.findById(examPaperId)
                .orElseThrow(() -> new RuntimeException("Exam_Paper not found with ID: " + examPaperId));

        byte[] fileCollection = examPaper.getFileCollectionPostman();
        if (fileCollection == null) {
            throw new RuntimeException("No fileCollectionPostman found in Exam_Paper with ID: " + examPaperId);
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode rootNode = (ObjectNode) objectMapper.readTree(fileCollection);
            ArrayNode items = (ArrayNode) rootNode.get("item");

            for (int i = 0; i < items.size(); i++) {
                ObjectNode item = (ObjectNode) items.get(i);
                if (item.has("request")) {
                    ObjectNode request = (ObjectNode) item.get("request");
                    if (request.has("url")) {
                        ObjectNode url = (ObjectNode) request.get("url");
                        String rawUrl = url.get("raw").asText();
                        String updatedRawUrl = rawUrl.replaceFirst("http://localhost:\\d+", "http://localhost:" + port);
                        url.put("raw", updatedRawUrl);
                        url.put("port", Integer.toString(port));
                    }
                }
            }

            // Convert updated JSON back to byte array and save it to sourceDetail
            byte[] updatedFileCollection = objectMapper.writeValueAsString(rootNode).getBytes(StandardCharsets.UTF_8);
            Source_Detail sourceDetail = sourceDetailRepository.findById(sourceDetailId)
                    .orElseThrow(() -> new RuntimeException("Source_Detail not found with ID: " + sourceDetailId));
            sourceDetail.setFileCollectionPostman(updatedFileCollection);
            sourceDetailRepository.save(sourceDetail);

            // Wait until fileCollectionPostman is confirmed as saved
            int maxRetries = 5;
            int retries = 0;
            while (retries < maxRetries) {
                sourceDetail = sourceDetailRepository.findById(sourceDetailId)
                        .orElseThrow(() -> new RuntimeException("Source_Detail not found with ID: " + sourceDetailId));

                if (sourceDetail.getFileCollectionPostman() != null) {
                    break; // Exit loop if the fileCollectionPostman is available
                }
                Thread.sleep(500); // Wait for a short time before retrying
                retries++;
            }

            if (sourceDetail.getFileCollectionPostman() == null) {
                throw new RuntimeException(
                        "fileCollectionPostman not properly saved for sourceDetailId: " + sourceDetailId);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to update fileCollectionPostman: " + e.getMessage(), e);
        }
    }

}
