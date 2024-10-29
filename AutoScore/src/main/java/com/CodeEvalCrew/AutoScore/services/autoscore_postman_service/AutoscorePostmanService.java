package com.CodeEvalCrew.AutoScore.services.autoscore_postman_service;

import java.io.BufferedReader;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import com.CodeEvalCrew.AutoScore.repositories.examdatabase_repository.IExamDatabaseRepository;
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

    @Override
    public List<StudentSourceInfoDTO> gradingFunction(Long examPaperId, int numberDeploy) {
        List<StudentSourceInfoDTO> studentSources = sourceDetailRepository
                .findBySource_ExamPaper_ExamPaperIdOrderByStudent_StudentId(examPaperId)
                .stream()
                .map(sourceDetail -> sourceDetailMapper.toDTO(sourceDetail))
                .collect(Collectors.toList());

        deleteAndCreateDatabaseByExamPaperId(examPaperId);

        try {
            deleteContainerAndImages();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete containers and images: " + e.getMessage());
        }

        processStudentSolutions(studentSources, examPaperId);

        return studentSources;
    }

    public void createFileCollectionPostman(Long examPaperId, Long sourceDetailId, int port) {
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
            byte[] updatedFileCollection = objectMapper.writeValueAsString(rootNode).getBytes(StandardCharsets.UTF_8);
            Source_Detail sourceDetail = sourceDetailRepository.findById(sourceDetailId)
                    .orElseThrow(() -> new RuntimeException("Source_Detail not found with ID: " + sourceDetailId));
            sourceDetail.setFileCollectionPostman(updatedFileCollection);
            sourceDetailRepository.save(sourceDetail);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update fileCollectionPostman: " + e.getMessage(), e);
        }
    }

    public void processStudentSolutions(List<StudentSourceInfoDTO> studentSources, Long examPaperId) {
        // Giới hạn chỉ lấy 3 phần tử đầu tiên
        List<StudentSourceInfoDTO> limitedStudentSources = studentSources.subList(0,
                Math.min(3, studentSources.size()));
        ExecutorService executor = Executors.newFixedThreadPool(limitedStudentSources.size());
        Map<Future<StudentDeployResult>, StudentSourceInfoDTO> futureToStudentSourceMap = new HashMap<>();
        List<StudentSourceInfoDTO> successfulDeployments = new ArrayList<>(); // List to track successful deployments


        for (int i = 0; i < limitedStudentSources.size(); i++) {
            StudentSourceInfoDTO studentSource = limitedStudentSources.get(i);
            Path dirPath = Paths.get(studentSource.getStudentSourceCodePath());
            int port = AutoscoreInitUtils.BASE_PORT + i;
            Long studentId = studentSource.getStudentId();

            Future<StudentDeployResult> future = executor.submit(() -> {
                try {
                    AutoscoreInitUtils.removeDockerFiles(dirPath);
                    var csprojAndVersion = AutoscoreInitUtils.findCsprojAndDotnetVersion(dirPath);

                    if (csprojAndVersion != null) {
                        AutoscoreInitUtils.createDockerfile(dirPath, csprojAndVersion.getKey(),
                                csprojAndVersion.getValue(), port);
                        AutoscoreInitUtils.createDockerCompose(dirPath, studentId, port);
                        findAndUpdateAppsettings(dirPath, examPaperId, port);
                        createFileCollectionPostman(examPaperId, studentSource.getSourceDetailId(), port);
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

        // Hiển thị kết quả triển khai cho mỗi sinh viên
        for (Map.Entry<Future<StudentDeployResult>, StudentSourceInfoDTO> entry : futureToStudentSourceMap.entrySet()) {
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
         // Run Newman for each successful deployment
         if (!successfulDeployments.isEmpty()) {
            StudentSourceInfoDTO firstSuccessfulStudent = successfulDeployments.get(0);

            Map<String, Integer> passedFunctionNames = storeAndRunPostmanCollection(
                firstSuccessfulStudent.getStudentId(), firstSuccessfulStudent.getSourceDetailId()
            );
        

           // Convert List to Map to get count of each function's success
        Map<String, Long> functionPassedCountMap = passedFunctionNames.entrySet().stream()
        .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingLong(Map.Entry::getValue)));

    System.out.println("Function passed count map: " + functionPassedCountMap);
    

        // Call saveScoreAndScoreDetail with the Map
        saveScoreAndScoreDetail(firstSuccessfulStudent.getStudentId(), examPaperId, functionPassedCountMap);
        
      
        } else {
            System.out.println("No successful deployments found to run Newman.");
        }

    }
   

    private Map<String, Integer> storeAndRunPostmanCollection(Long studentId, Long sourceDetailId) {
        Map<String, Integer> functionResults = new HashMap<>();
        String currentFunction = null;
        int passCount = 0;

        try {
            Path studentDir = Paths.get("D:/Desktop/all collection postman", String.valueOf(studentId));
            Files.createDirectories(studentDir);

            // Đường dẫn đến file Postman collection
            Path postmanFilePath = studentDir.resolve("fileCollectionPostman.json");

            String newmanPath = "C:/Users/Admin/AppData/Roaming/npm/newman.cmd";
            System.out.println("Running Newman for studentId: " + studentId);

            ProcessBuilder processBuilder = new ProcessBuilder(
                    newmanPath, "run", postmanFilePath.toString()
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Raw Output: " + line);
                    line = line.trim();

                    // Kiểm tra xem dòng bắt đầu với biểu tượng '→' để tìm tên hàm
                    if (line.startsWith("→")) {
                        // Nếu đã có một hàm trước đó, lưu kết quả của nó
                        if (currentFunction != null) {
                            functionResults.put(currentFunction, passCount);
                        }
                        // Thiết lập hàm hiện tại và reset passCount
                        currentFunction = line.split("\\s+")[1];
                        passCount = 0;  // Reset đếm dấu tích cho hàm mới

                    } else if (line.startsWith("√")) {
                        // Đếm dấu tích (√) cho kiểm thử thành công
                        passCount++;

                    } else if (line.matches("^\\d+\\..*")) {
                        // Kiểm tra thất bại không có dấu √ sẽ không tăng passCount
                        if (!line.contains("√")) {
                            passCount = 0; // Reset nếu có kiểm thử thất bại trong hàm
                        }
                    }
                }

                // Lưu kết quả của hàm cuối cùng nếu có
                if (currentFunction != null) {
                    functionResults.put(currentFunction, passCount);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Newman executed successfully for studentId: " + studentId);
            } else {
                System.err.println("Newman execution failed with exit code: " + exitCode + " for studentId: " + studentId);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error running Newman or storing Postman collection for studentId " + studentId + ": " + e.getMessage());
        }

        // In kết quả cuối cùng
        functionResults.forEach((function, count) -> {
            System.out.println("noPmtestAchieve for " + function + ": " + count);
        });

        return functionResults;
    }
    


public void saveScoreAndScoreDetail(Long studentId, Long examPaperId, Map<String, Long> functionPassedCountMap) {
    // Retrieve student and exam paper entities
    Student student = studentRepository.findById(studentId).orElse(null);
    Exam_Paper examPaper = examPaperRepository.findById(examPaperId).orElse(null);

    if (student == null || examPaper == null) {
        System.err.println("Student hoặc Exam Paper không tồn tại.");
        return;
    }

    // Create a Score object and set its properties
    Score score = new Score();
    score.setStudent(student);
    score.setExamPaper(examPaper);
    score.setGradedAt(LocalDateTime.now());
    score.setFlag(true);
    score.setOrganization(student.getOrganization());

    scoreRepository.save(score);

    // Process each question and its Postman functions in the exam paper
    for (Exam_Question question : examPaper.getExamQuestions()) {
        for (Postman_For_Grading postmanFunction : question.getPostmanForGradingEntries()) {
            Score_Detail scoreDetail = new Score_Detail();
            scoreDetail.setScore(score);
            scoreDetail.setExamQuestion(question);
            scoreDetail.setPostmanFunctionName(postmanFunction.getPostmanFunctionName());
            scoreDetail.setScoreOfFunction(postmanFunction.getScoreOfFunction());
            scoreDetail.setTotalPmtest(postmanFunction.getTotalPmTest());

            // Calculate noPmtestAchieve based on the passed test count from functionPassedCountMap
            Long noPmtestAchieve = functionPassedCountMap.getOrDefault(postmanFunction.getPostmanFunctionName(), 0L);
            scoreDetail.setNoPmtestAchieve(noPmtestAchieve);

            // Calculate scoreAchieve using the formula: (noPmtestAchieve / totalPmtest) * scoreOfFunction
            Float totalPmtest = postmanFunction.getTotalPmTest().floatValue();
            Float scoreAchieve = (noPmtestAchieve / totalPmtest) * postmanFunction.getScoreOfFunction();
            scoreDetail.setScoreAchieve(scoreAchieve);

            // Save the score detail
            scoreDetailRepository.save(scoreDetail);
            System.out.println("noPmtestAchieve for " + postmanFunction.getPostmanFunctionName() + ": " + noPmtestAchieve);
        }
        
    }
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
            score.setFlag(false);
            score.setOrganization(student.getOrganization());

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
                System.out.println("Removing container " + container.getNames()[0] + " ("
                        + container.getId().substring(0, 12) + ")");
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

        } catch (DockerException e) {
            e.printStackTrace();
            throw new RuntimeException("Docker operation failed: " + e.getMessage());
        } finally {
            dockerClient.close(); // Close Docker client
        }
    }
}
