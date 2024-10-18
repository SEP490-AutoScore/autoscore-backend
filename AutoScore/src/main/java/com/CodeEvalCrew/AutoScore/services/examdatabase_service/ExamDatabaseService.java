package com.CodeEvalCrew.AutoScore.services.examdatabase_service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Database;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.examdatabase_repository.IExamDatabaseRepository;
import com.CodeEvalCrew.AutoScore.utils.Util;

@Service
public class ExamDatabaseService implements IExamDatabaseService {

     @Autowired
    private IExamDatabaseRepository examDatabaseRepository;
@Autowired
    private IExamPaperRepository examPaperRepository;
    // Cấu hình kết nối với SQL Server
    private final String url = "jdbc:sqlserver://ADMIN-PC\\SQLEXPRESS;databaseName=master;user=sa;password=1234567890;encrypt=false;trustServerCertificate=true;";
    private final String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    // Import file SQL và thực hiện các bước xử lý
    public String importSqlFile(MultipartFile file, MultipartFile imageFile,  Long examPaperId) throws Exception {
        // Nạp driver JDBC của SQL Server
        try {
            Class.forName(driver);
            System.out.println("Driver SQL Server đã nạp thành công!");
        } catch (ClassNotFoundException e) {
            throw new Exception("Không tìm thấy driver SQL Server: " + e.getMessage());
        }

        // Đọc nội dung file .sql
        StringBuilder sql = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sql.append(line).append("\n");
            }
        }

        // Lấy tên database từ file .sql
        String dbName = extractDatabaseName(sql.toString());
        if (dbName == null) {
            throw new Exception("Không tìm thấy tên database trong file .sql");
        }

        // Kết nối đến SQL Server (master database)
        try (Connection conn = DriverManager.getConnection(url)) {
            conn.setAutoCommit(false); // Bắt đầu transaction
            Statement stmt = conn.createStatement();

            try {
                // Kiểm tra và xóa database nếu tồn tại
                dropDatabaseIfExists(stmt, dbName);

                // Tạo lại database
                System.out.println("Tạo database: " + dbName);
                stmt.execute("CREATE DATABASE " + dbName);

                // Chuyển sang USE database vừa tạo
                stmt.execute("USE " + dbName);
                System.out.println("Đã chuyển sang database: " + dbName);

                // Thực thi các lệnh SQL khác từ file .sql (ngoại trừ lệnh CREATE DATABASE và
                // USE)
                executeSqlStatements(stmt, sql.toString().replaceFirst("(?i)CREATE DATABASE\\s+[a-zA-Z0-9_]+\\s*GO", "")
                        .replaceFirst("(?i)USE\\s+[a-zA-Z0-9_]+\\s*GO", ""), dbName);

                // Commit transaction sau khi thành công
                conn.commit();

// Lấy dữ liệu từ file ảnh (nếu có)
byte[] imageData = null;
if (imageFile == null || imageFile.isEmpty()) {
    throw new Exception("File ảnh không hợp lệ hoặc không được cung cấp."); // Throw an exception if image file is invalid
} 

// Kiểm tra định dạng file (chỉ chấp nhận PNG và JPEG)
String contentType = imageFile.getContentType();
if (!"image/png".equals(contentType) && !"image/jpeg".equals(contentType) && !"image/jpg".equals(contentType)) {
    throw new Exception("Chỉ chấp nhận file ảnh PNG, JPEG, JPG");
}

// Đọc dữ liệu của file ảnh
imageData = imageFile.getBytes();
System.out.println("Image file size: " + imageData.length + " bytes");  // Log the image size


// Fetch the exam paper entity using examPaperId
        Exam_Paper examPaper = examPaperRepository.findById(examPaperId)
            .orElseThrow(() -> new Exception("Exam paper not found with id: " + examPaperId));

// Lưu thông tin vào bảng Exam_Database
Long authenticatedUserId = Util.getAuthenticatedAccountId(); // Lấy ID người dùng hiện tại
LocalDateTime now = Util.getCurrentDateTime(); // Lấy thời gian hiện tại

Exam_Database examDatabase = new Exam_Database();
examDatabase.setDatabaseScript(sql.toString()); // Lưu nội dung file .sql
examDatabase.setDatabaseName(dbName); // Lưu tên database
examDatabase.setDatabaseImage(imageData); // Lưu hình ảnh dưới dạng byte[]
examDatabase.setStatus(true); // Đặt status là true
examDatabase.setCreatedAt(now); // Thời gian hiện tại
examDatabase.setCreatedBy(authenticatedUserId); // ID người dùng
examDatabase.setExamPaper(examPaper);

 // Save the entity in the database
examDatabaseRepository.save(examDatabase);
                
                return "Database " + dbName + " đã được tạo và dữ liệu đã được import.";

            } catch (SQLException e) {
                conn.rollback(); // Rollback nếu có lỗi
                throw new Exception("Transaction bị lỗi và đã rollback: " + e.getMessage());
            }

        } catch (SQLException e) {
            throw new Exception("Lỗi kết nối hoặc thực thi SQL: " + e.getMessage());
        }
    }

    // Hàm để kiểm tra và xóa database nếu tồn tại
    private void dropDatabaseIfExists(Statement stmt, String dbName) throws SQLException {
        String checkDbQuery = "IF EXISTS (SELECT name FROM sys.databases WHERE name = '" + dbName + "') " +
                "BEGIN " +
                "   ALTER DATABASE [" + dbName + "] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; " +
                "   DROP DATABASE [" + dbName + "]; " +
                "END";
        try {
            stmt.execute(checkDbQuery); // Xóa database nếu tồn tại
            System.out.println("Đã kiểm tra và xóa database nếu tồn tại: " + dbName);
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa database: " + e.getMessage());
            throw e;
        }
    }

    // Lấy tên database từ nội dung file SQL
    private String extractDatabaseName(String sqlContent) {
        Pattern pattern = Pattern.compile("CREATE DATABASE\\s+([a-zA-Z0-9_]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sqlContent);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    // Thực thi các câu lệnh SQL từ file .sql
    private void executeSqlStatements(Statement stmt, String sqlContent, String dbName) throws SQLException {
        // Chuyển sang sử dụng database đích
        stmt.execute("USE " + dbName);
        System.out.println("Đã chuyển sang database: " + dbName);

        // Loại bỏ tất cả các lệnh USE khỏi nội dung file SQL
        sqlContent = sqlContent.replaceAll("(?i)\\bUSE\\b\\s+[a-zA-Z0-9_]+\\s*GO", "");

        // Chia các câu lệnh SQL theo từ khóa GO
        String[] sqlStatements = sqlContent.split("(?i)\\bGO\\b");

        // Thực thi từng câu lệnh SQL
        for (String sql : sqlStatements) {
            sql = sql.trim();

            // Bỏ qua các câu lệnh rỗng
            if (!sql.isEmpty()) {
                try {
                    System.out.println("Đang thực thi câu lệnh: " + sql);
                    stmt.execute(sql); // Thực thi từng lệnh SQL
                } catch (SQLException e) {
                    System.err.println("Lỗi khi thực thi câu lệnh: " + sql);
                    throw e; // Ghi log lỗi và ném ngoại lệ để rollback transaction
                }
            }
        }
    }

  

}
