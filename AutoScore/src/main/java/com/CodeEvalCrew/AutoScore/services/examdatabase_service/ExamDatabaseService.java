package com.CodeEvalCrew.AutoScore.services.examdatabase_service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamDatabaseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Database;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.examdatabase_repository.IExamDatabaseRepository;
import com.CodeEvalCrew.AutoScore.utils.PathUtil;
import com.CodeEvalCrew.AutoScore.utils.Util;

@Service
public class ExamDatabaseService implements IExamDatabaseService {

    @Autowired
    private IExamDatabaseRepository examDatabaseRepository;

    @Autowired
    private IExamPaperRepository examPaperRepository;

    // private final String url = "jdbc:sqlserver://ADMIN-PC\\SQLEXPRESS;databaseName=master;user=sa;password=1234567890;encrypt=false;trustServerCertificate=true;";
    // private final String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";



      public String importSqlFile(MultipartFile file, MultipartFile imageFile, Long examPaperId, String databaseNote, String databaseDescription) throws Exception {
        try {
            Class.forName(PathUtil.DATABASE_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new Exception("SQL Server driver not found: " + e.getMessage());
        }

        // Chuyển đổi nội dung file .sql thành chuỗi và lưu vào `databaseScript`
        String databaseScript;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder scriptBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                scriptBuilder.append(line).append("\n");
            }
            databaseScript = scriptBuilder.toString();
        }

        String dbName = extractDatabaseName(databaseScript);
        if (dbName == null) {
            throw new Exception("Database name not found in .sql file");
        }

        try (Connection conn = DriverManager.getConnection(PathUtil.DATABASE_URL)) {
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();

            try {
                dropDatabaseIfExists(stmt, dbName);

                stmt.execute("CREATE DATABASE " + dbName);
                stmt.execute("USE " + dbName);

                executeSqlStatements(stmt, databaseScript.replaceFirst("(?i)CREATE DATABASE\\s+[a-zA-Z0-9_]+\\s*GO", "")
                        .replaceFirst("(?i)USE\\s+[a-zA-Z0-9_]+\\s*GO", ""), dbName);

                conn.commit();

                byte[] imageData;
                if (imageFile == null || imageFile.isEmpty()) {
                    throw new Exception("Invalid or missing image file.");
                }

                String contentType = imageFile.getContentType();
                if (!"image/png".equals(contentType) && !"image/jpeg".equals(contentType) && !"image/jpg".equals(contentType)) {
                    throw new Exception("Only PNG, JPEG, JPG files are accepted");
                }

                imageData = imageFile.getBytes();

                Exam_Paper examPaper = examPaperRepository.findById(examPaperId)
                        .orElseThrow(() -> new Exception("Exam paper not found with id: " + examPaperId));

                Long authenticatedUserId = Util.getAuthenticatedAccountId();
                LocalDateTime now = Util.getCurrentDateTime();

                Exam_Database examDatabase = new Exam_Database();
                examDatabase.setDatabaseScript(databaseScript); // Lưu chuỗi SQL vào `databaseScript`
                examDatabase.setDatabaseName(dbName);
                examDatabase.setDatabaseImage(imageData);
                // examDatabase.setStatus(true);
                examDatabase.setCreatedAt(now);
                examDatabase.setCreatedBy(authenticatedUserId);
                examDatabase.setExamPaper(examPaper);
                examDatabase.setDatabaseNote(databaseNote); 
                examDatabase.setDatabaseDescription(databaseDescription); 

                examDatabaseRepository.save(examDatabase);

                return "Database " + dbName + " has been created and data has been imported.";

            } catch (SQLException e) {
                conn.rollback();
                throw new Exception("Transaction failed and rolled back: " + e.getMessage());
            }

        } catch (SQLException e) {
            throw new Exception("SQL connection or execution error: " + e.getMessage());
        }
    }

    private void dropDatabaseIfExists(Statement stmt, String dbName) throws SQLException {
        String checkDbQuery = "IF EXISTS (SELECT name FROM sys.databases WHERE name = '" + dbName + "') " +
                "BEGIN " +
                "   ALTER DATABASE [" + dbName + "] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; " +
                "   DROP DATABASE [" + dbName + "]; " +
                "END";
        stmt.execute(checkDbQuery);
    }

    private String extractDatabaseName(String sqlContent) {
        Pattern pattern = Pattern.compile("CREATE DATABASE\\s+([a-zA-Z0-9_]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sqlContent);
        return matcher.find() ? matcher.group(1) : null;
    }

    private void executeSqlStatements(Statement stmt, String sqlContent, String dbName) throws SQLException {
        stmt.execute("USE " + dbName);
        String[] sqlStatements = sqlContent.split("(?i)\\bGO\\b");

        for (String sql : sqlStatements) {
            sql = sql.trim();
            if (!sql.isEmpty()) {
                stmt.execute(sql);
            }
        }
    }

    public String updateSqlFile(MultipartFile sqlFile, MultipartFile imageFile, Long examPaperId, String databaseNote, String databaseDescription ) throws Exception {
        try {
            Class.forName(PathUtil.DATABASE_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new Exception("SQL Server driver not found: " + e.getMessage());
        }
    
        // Chuyển đổi nội dung file .sql thành chuỗi và lưu vào `databaseScript`
        String databaseScript;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(sqlFile.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder scriptBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                scriptBuilder.append(line).append("\n");
            }
            databaseScript = scriptBuilder.toString();
        }
    
        String dbName = extractDatabaseName(databaseScript);
        if (dbName == null) {
            throw new Exception("Database name not found in .sql file");
        }
    
        // Kiểm tra và xóa các thông tin databaseImage, databaseName, và databaseScript nếu có
        Exam_Database existingDatabase = examDatabaseRepository.findByExamPaper_ExamPaperId(examPaperId).orElse(null);
        if (existingDatabase != null) {
            existingDatabase.setDatabaseImage(null);
            existingDatabase.setDatabaseName(null);
            existingDatabase.setDatabaseScript(null);
            examDatabaseRepository.save(existingDatabase);
        }
    
        try (Connection conn = DriverManager.getConnection(PathUtil.DATABASE_URL)) {
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
    
            try {
                dropDatabaseIfExists(stmt, dbName);
    
                stmt.execute("CREATE DATABASE " + dbName);
                stmt.execute("USE " + dbName);
    
                executeSqlStatements(stmt, databaseScript.replaceFirst("(?i)CREATE DATABASE\\s+[a-zA-Z0-9_]+\\s*GO", "")
                        .replaceFirst("(?i)USE\\s+[a-zA-Z0-9_]+\\s*GO", ""), dbName);
    
                conn.commit();
    
                // Xử lý tệp ảnh
                if (imageFile == null || imageFile.isEmpty()) {
                    throw new Exception("Invalid or missing image file.");
                }
    
                String contentType = imageFile.getContentType();
                if (!"image/png".equals(contentType) && !"image/jpeg".equals(contentType) && !"image/jpg".equals(contentType)) {
                    throw new Exception("Only PNG, JPEG, JPG files are accepted");
                }
    
                byte[] imageData = imageFile.getBytes();
    
                // Lấy thông tin về bài thi
                Exam_Paper examPaper = examPaperRepository.findById(examPaperId)
                        .orElseThrow(() -> new Exception("Exam paper not found with id: " + examPaperId));
    
                Long authenticatedUserId = Util.getAuthenticatedAccountId();
                LocalDateTime now = Util.getCurrentDateTime();
    
                Exam_Database updatedDatabase = existingDatabase != null ? existingDatabase : new Exam_Database();
                updatedDatabase.setDatabaseScript(databaseScript);
                updatedDatabase.setDatabaseName(dbName);
                updatedDatabase.setDatabaseImage(imageData);
                // updatedDatabase.setStatus(true);
                updatedDatabase.setUpdatedAt(now);
                updatedDatabase.setUpdatedBy(authenticatedUserId);
                updatedDatabase.setExamPaper(examPaper);
                updatedDatabase.setDatabaseNote(databaseNote); 
                updatedDatabase.setDatabaseDescription(databaseDescription); 
    
                examDatabaseRepository.save(updatedDatabase);
    
                return "Database " + dbName + " has been updated successfully.";
    
            } catch (SQLException e) {
                conn.rollback();
                throw new Exception("Transaction failed and rolled back: " + e.getMessage());
            }
    
        } catch (SQLException e) {
            throw new Exception("SQL connection or execution error: " + e.getMessage());
        }
    }
    // @Override
    // public ExamDatabaseDTO getExamDatabaseByExamPaperId(Long examPaperId) {
    //     Exam_Database examDatabase = examDatabaseRepository.findByExamPaper_ExamPaperId(examPaperId)
    //             .orElseThrow(() -> new IllegalArgumentException("Exam database not found for examPaperId: " + examPaperId));
    
    //     return new ExamDatabaseDTO(
    //             examDatabase.getExamDatabaseId(),
    //             examDatabase.getDatabaseScript(),
    //             examDatabase.getDatabaseDescription(),
    //             examDatabase.getDatabaseName(),
    //             examDatabase.getDatabaseImage(),
    //             examDatabase.getDatabaseNote(),
    //             examDatabase.getStatus(),
    //             examDatabase.getCreatedAt(),
    //             examDatabase.getCreatedBy(),
    //             examDatabase.getUpdatedAt(),
    //             examDatabase.getUpdatedBy(),
    //             examDatabase.getDeletedAt(),
    //             examDatabase.getDeletedBy(),
    //             examDatabase.getExamPaper().getExamPaperId()
    //     );
    // }
    
    @Override
    public Optional<ExamDatabaseDTO> getExamDatabaseByExamPaperId(Long examPaperId) {
        return examDatabaseRepository.findByExamPaper_ExamPaperId(examPaperId)
                .map(examDatabase -> new ExamDatabaseDTO(
                        examDatabase.getExamDatabaseId(),
                        examDatabase.getDatabaseScript(),
                        examDatabase.getDatabaseDescription(),
                        examDatabase.getDatabaseName(),
                        examDatabase.getDatabaseImage(),
                        examDatabase.getDatabaseNote(),
                        examDatabase.getStatus(),
                        examDatabase.getCreatedAt(),
                        examDatabase.getCreatedBy(),
                        examDatabase.getUpdatedAt(),
                        examDatabase.getUpdatedBy(),
                        examDatabase.getDeletedAt(),
                        examDatabase.getDeletedBy(),
                        examDatabase.getExamPaper().getExamPaperId()
                ));
    }
    

}
