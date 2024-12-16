package com.CodeEvalCrew.AutoScore.services.log_service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.Entity.Employee;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.models.Entity.Log;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IEmployeeRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.log_repository.LogRepository;

@Service
public class LogService implements ILogService {

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private IExamPaperRepository examPaperRepository;

    @Autowired
    private IEmployeeRepository employeeRepository;

    @Override
    public String exportLogToFile(Long examPaperId) throws IOException {
        // Tìm Exam_Paper theo examPaperId
        Optional<Exam_Paper> examPaperOpt = examPaperRepository.findById(examPaperId);
        if (examPaperOpt.isPresent()) {
            Exam_Paper examPaper = examPaperOpt.get();
            String examPaperCode = examPaper.getExamPaperCode();
            String examCode = examPaper.getExam().getExamCode();
    
            // Tìm Log tương ứng với examPaperId
            Optional<Log> logOpt = logRepository.findByExamPaper_ExamPaperId(examPaperId);
            if (logOpt.isPresent()) {
                Log log = logOpt.get();
                String allData = log.getAllData();
    
                // Thay thế accountId bằng fullName từ Employee
                allData = replaceAccountIdWithFullName(allData);
    
                // Tạo tên file .txt với tên Log_of_{examPaperCode}-{examCode}.txt
                String fileName = "Log_of_" + examPaperCode + "-" + examCode + ".txt";
    
                // Tạo file tại vị trí cần thiết
                File file = new File(fileName);
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(allData);
                 
                } catch (IOException e) {
                    throw new IOException("Error writing to file", e);
                }
    
                // Trả về tên file thay vì trả về file trực tiếp
                return fileName;
            } else {
                throw new IOException("Log data not found for examPaperId: " + examPaperId);
            }
        } else {
            throw new IOException("ExamPaper not found for examPaperId: " + examPaperId);
        }
    }
    

    private String replaceAccountIdWithFullName(String allData) {
        // Sử dụng regex để tìm tất cả các mẫu "Account [accountId]"
        Pattern pattern = Pattern.compile("Account \\[(\\d+)]");
        Matcher matcher = pattern.matcher(allData);
        
        StringBuffer updatedData = new StringBuffer();
        
        // Lặp qua tất cả các kết quả tìm được
        while (matcher.find()) {
            String accountIdStr = matcher.group(1); // Lấy accountId trong dấu []
            try {
                Long accountId = Long.parseLong(accountIdStr); // Chuyển accountId thành Long
              
                
                // Tìm Employee từ accountId
                Employee employee = employeeRepository.findByAccount_AccountId(accountId);
                String replacement = (employee != null) ? employee.getFullName() : "Unknown User";
              
                
                // Thay thế "Account [accountId]" bằng "Account [fullName]"
                matcher.appendReplacement(updatedData, "Account [" + replacement + "]");
            } catch (NumberFormatException e) {
                System.err.println("Invalid accountId format: " + accountIdStr);
                matcher.appendReplacement(updatedData, matcher.group(0)); // Giữ nguyên nếu lỗi
            }
        }
        matcher.appendTail(updatedData); // Thêm phần còn lại của chuỗi
        
        return updatedData.toString();
    }
    
    
}