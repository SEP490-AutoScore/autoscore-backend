package com.CodeEvalCrew.AutoScore.services.autoscore_postman_service;

import java.io.IOException;
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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.mappers.ScoreMapper;
import com.CodeEvalCrew.AutoScore.mappers.SourceDetailMapperforAutoscore;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamPaperDTOforAutoscore;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.StudentDTOforAutoscore;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.StudentDeployResult;
import com.CodeEvalCrew.AutoScore.models.DTO.StudentSourceInfoDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Database;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.models.Entity.Score;
import com.CodeEvalCrew.AutoScore.models.Entity.Source_Detail;
import com.CodeEvalCrew.AutoScore.models.Entity.Student;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.examdatabase_repository.IExamDatabaseRepository;
import com.CodeEvalCrew.AutoScore.repositories.score_repository.ScoreRepository;
import com.CodeEvalCrew.AutoScore.repositories.source_repository.SourceDetailRepository;
import com.CodeEvalCrew.AutoScore.repositories.source_repository.SourceRepository;
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



    @Override
    public List<StudentSourceInfoDTO> gradingFunction(Long examPaperId, int numberDeploy) {
        List<StudentSourceInfoDTO> studentSources = sourceDetailRepository.findBySource_ExamPaper_ExamPaperIdOrderByStudent_StudentId(examPaperId)
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

    public void updateAppsettingsJson(Path filePath, Long examPaperId, int port) throws IOException {
        // Đọc tệp JSON dưới dạng chuỗi để có thể dùng biểu thức chính quy
        String content = Files.readString(filePath, StandardCharsets.UTF_8);
    
        // Tạo ObjectMapper để thao tác với JSON
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootNode = (ObjectNode) objectMapper.readTree(content);
    
        // Fetch database name dynamically based on examPaperId
        String databaseName = examDatabaseRepository.findDatabaseNameByExamPaperId(examPaperId);
    
        // Cập nhật ConnectionStrings với database name mới
        if (rootNode.has("ConnectionStrings")) {
            ObjectNode connectionStringsNode = (ObjectNode) rootNode.get("ConnectionStrings");
            connectionStringsNode.fieldNames().forEachRemaining(key -> {
                connectionStringsNode.put(key, String.join(";",
                        "Server=192.168.2.16\\SQLEXPRESS",
                        "uid=sa",
                        "pwd=1234567890",
                        "database=" + databaseName,
                        "TrustServerCertificate=True"
                ));
            });
        }
    
        // Ghi lại JSON đã chỉnh sửa vào chuỗi để có thể dùng biểu thức chính quy
        content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
    
        // Tìm và thay thế chuỗi "Url" với cổng mới
        String portPattern = "\"Url\"\\s*:\\s*\"http://\\*:[0-9]+\"";
        String replacement = "\"Url\": \"http://*:" + port + "\"";
        content = content.replaceAll(portPattern, replacement);
    
        // Ghi lại nội dung vào file
        Files.writeString(filePath, content, StandardCharsets.UTF_8);
    }
    

    public void findAndUpdateAppsettings(Path dirPath, Long examPaperId, int port) throws IOException {
        try (Stream<Path> folders = Files.walk(dirPath, 1)) {
            // Find directories that contain a Program.cs file
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

            // Search for appsettings.json in the found directories
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
            // Load SQL Server JDBC driver
            Class.forName(DB_DRIVER);
    
            // Establish connection to SQL Server
            try (Connection connection = DriverManager.getConnection(DB_URL);
                 Statement statement = connection.createStatement()) {
    
                // Fetch the database name and Exam_Database based on examPaperId
                String databaseName = examDatabaseRepository.findDatabaseNameByExamPaperId(examPaperId);
                Exam_Database examDatabase = examDatabaseRepository.findByExamPaperExamPaperId(examPaperId);
    
                if (examDatabase != null) {
                    Long examDatabaseId = examDatabase.getExamDatabaseId();
                    System.out.println("Found Exam_Database ID: " + examDatabaseId);
    
                    if (databaseName != null && !databaseName.isEmpty()) {
                        // Drop the database if it exists
                        String sql = "IF EXISTS (SELECT name FROM sys.databases WHERE name = '" + databaseName + "') " +
                                     "BEGIN " +
                                     "   ALTER DATABASE [" + databaseName + "] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; " +
                                     "   DROP DATABASE [" + databaseName + "]; " +
                                     "END";
                        statement.executeUpdate(sql);
                        System.out.println("Database " + databaseName + " has been deleted.");
                    }
    
                    // Create the database using the file from Exam_Database
                    if (examDatabase.getDatabaseFile() != null) {
                        String createDatabaseSQL = new String(examDatabase.getDatabaseFile());
    
                        // Remove 'GO' statements
                        String[] sqlCommands = createDatabaseSQL.split("(?i)\\bGO\\b");
    
                        // Execute each statement separately
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
    
public void createFileCollectionPostman(Long examPaperId, Long sourceDetailId, int port) {
    // Retrieve the Exam_Paper with the specified examPaperId
    Exam_Paper examPaper = examPaperRepository.findById(examPaperId)
        .orElseThrow(() -> new RuntimeException("Exam_Paper not found with ID: " + examPaperId));

    byte[] fileCollection = examPaper.getFileCollectionPostman();
    if (fileCollection == null) {
        throw new RuntimeException("No fileCollectionPostman found in Exam_Paper with ID: " + examPaperId);
    }

    try {
        // Convert byte[] to JSON node
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootNode = (ObjectNode) objectMapper.readTree(fileCollection);

        // Navigate to "item" array
        ArrayNode items = (ArrayNode) rootNode.get("item");

        for (int i = 0; i < items.size(); i++) {
            ObjectNode item = (ObjectNode) items.get(i);
            if (item.has("request")) {
                ObjectNode request = (ObjectNode) item.get("request");
                if (request.has("url")) {
                    ObjectNode url = (ObjectNode) request.get("url");

                    // Update the port in "url.raw"
                    String rawUrl = url.get("raw").asText();
                    String updatedRawUrl = rawUrl.replaceFirst("http://localhost:\\d+", "http://localhost:" + port);
                    url.put("raw", updatedRawUrl);

                    // Update the port in "url.port"
                    url.put("port", Integer.toString(port));
                }
            }
        }

        // Convert modified JSON node back to byte[]
        byte[] updatedFileCollection = objectMapper.writeValueAsString(rootNode).getBytes(StandardCharsets.UTF_8);

        // Retrieve Source_Detail to update
        Source_Detail sourceDetail = sourceDetailRepository.findById(sourceDetailId)
            .orElseThrow(() -> new RuntimeException("Source_Detail not found with ID: " + sourceDetailId));

        // Update Source_Detail with the modified fileCollectionPostman
        sourceDetail.setFileCollectionPostman(updatedFileCollection);
        sourceDetailRepository.save(sourceDetail);

    } catch (Exception e) {
        throw new RuntimeException("Failed to update fileCollectionPostman: " + e.getMessage(), e);
    }
}



public void processStudentSolutions(List<StudentSourceInfoDTO> studentSources, Long examPaperId) {
    // Giới hạn chỉ lấy 3 phần tử đầu tiên
    List<StudentSourceInfoDTO> limitedStudentSources = studentSources.subList(0, Math.min(3, studentSources.size()));
    
    // Sử dụng số lượng luồng theo số lượng studentSources giới hạn
    ExecutorService executor = Executors.newFixedThreadPool(limitedStudentSources.size());
    List<Future<StudentDeployResult>> futures = new ArrayList<>(); // Danh sách để lưu trữ kết quả

    for (int i = 0; i < limitedStudentSources.size(); i++) {
        StudentSourceInfoDTO studentSource = limitedStudentSources.get(i);
        Path dirPath = Paths.get(studentSource.getStudentSourceCodePath());
        int port = AutoscoreInitUtils.BASE_PORT + i;
        Long studentId = studentSource.getStudentId();

        // Gửi công việc tới ExecutorService và lưu trữ Future
        Future<StudentDeployResult> future = executor.submit(() -> {
            try {
                AutoscoreInitUtils.removeDockerFiles(dirPath);
                var csprojAndVersion = AutoscoreInitUtils.findCsprojAndDotnetVersion(dirPath);

                if (csprojAndVersion != null) {
                    AutoscoreInitUtils.createDockerfile(dirPath, csprojAndVersion.getKey(), csprojAndVersion.getValue(), port);
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

        futures.add(future);
    }

    executor.shutdown();
    try {
        executor.awaitTermination(60, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }

    // Hiển thị kết quả triển khai cho mỗi sinh viên
    for (Future<StudentDeployResult> future : futures) {
        try {
            StudentDeployResult result = future.get();
            System.out.println(result.getMessage() + " cho studentId: " + result.getStudentId());

            if (!result.isSuccessful()) {
                recordFailure(result.getStudentId(), examPaperId, "Can not deploy docker");
            }

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
private void recordFailure(Long studentId, Long examPaperId, String reason) {
    // Fetch the Student entity based on the studentId
    Student student = scoreRepository.findStudentById(studentId); // Assuming you have a method to fetch a Student

    if (student != null && student.getOrganization() != null) {
        Score score = new Score();
        score.setStudent(student); // Set the student
        
        // Kiểm tra nếu examPaper đã được định nghĩa trước đó
        Exam_Paper examPaper = new Exam_Paper(); // <-- Lỗi ở đây nếu bạn đã khai báo examPaper trước đó
        examPaper.setExamPaperId(examPaperId); // Set exam paper ID

        score.setExamPaper(examPaper); // Sử dụng đối tượng examPaper đã tạo
        score.setTotalScore(0.0f);
        score.setGradedAt(LocalDateTime.now());
        score.setReason(reason);
        score.setFlag(false); // Assuming `flag` indicates whether the deployment was successful
        score.setOrganization(student.getOrganization()); // Set the organization

        scoreRepository.save(score);
    } else {
        System.err.println("Student or Organization not found for studentId: " + studentId);
        // Handle this case appropriately, maybe log an error or throw an exception
    }
}

// private void recordFailure(Long studentId, Long examPaperId, String reason) {
//  // Tạo StudentDTO với studentId
// StudentDTOforAutoscore studentDTO = new StudentDTOforAutoscore();
// studentDTO.setStudentId(studentId);

// // Tạo ExamPaperDTO với examPaperId
// ExamPaperDTOforAutoscore examPaperDTO = new ExamPaperDTOforAutoscore();
// examPaperDTO.setExamPaperId(examPaperId);

// // Sử dụng mapper để chuyển đổi từ DTOs sang thực thể
// Student student = ScoreMapper.INSTANCE.toStudent(studentDTO);
// Exam_Paper examPaper = ScoreMapper.INSTANCE.toExamPaper(examPaperDTO);

// // Đảm bảo rằng student và examPaper không null trước khi sử dụng
// if (student != null && examPaper != null) {
//     // Tiến hành lưu Score với student và examPaper đã được khởi tạo
//     Score score = new Score();
//     score.setStudent(student); 
//     score.setExamPaper(examPaper); 
//     score.setTotalScore(0.0f);
//     score.setGradedAt(LocalDateTime.now());
//     score.setReason(reason);
//     score.setFlag(false); 
//     score.setOrganization(student.getOrganization()); // Giả sử organization đã được thiết lập trong Student

//     scoreRepository.save(score);
// } else {
//     System.err.println("Failed to create student or exam paper from DTOs.");
// }

// }



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
    
        } catch (DockerException e) {
            e.printStackTrace();
            throw new RuntimeException("Docker operation failed: " + e.getMessage());
        } finally {
            dockerClient.close(); // Close Docker client
        }
    }
}
