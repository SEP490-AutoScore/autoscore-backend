package com.CodeEvalCrew.AutoScore.services.student_service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam;
import com.CodeEvalCrew.AutoScore.models.Entity.Organization;
import com.CodeEvalCrew.AutoScore.models.Entity.Student;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamRepository;
import com.CodeEvalCrew.AutoScore.repositories.organization_repository.IOrganizationRepoistory;

@Service
public class ExcelService {
    private final IOrganizationRepoistory organizationRepoistory;
    private final IExamRepository examRepository;

    public ExcelService(IOrganizationRepoistory organizationRepoistory, IExamRepository examRepository) {
        this.organizationRepoistory = organizationRepoistory;
        this.examRepository = examRepository;
    }


    public List<Student> importExcelFile(MultipartFile file, Long examId, Long organizationId) throws IOException {
        List<Student> students = new ArrayList<>();  

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
    
            // Bỏ qua dòng đầu tiên và dòng tiêu đề thừa
            if (rows.hasNext()) rows.next(); // Dòng tiêu đề
            if (rows.hasNext()) rows.next(); // Dòng thừa "1 1 Student Code Email"
    
            if (organizationId == null || organizationId == 0 || examId == null || examId == 0) {
                return null;
            }

            Organization organization = organizationRepoistory.findById(organizationId).get();
            Exam exam = examRepository.findById(examId).get();

            if (organization == null || exam == null) {
                return null;
            }

            while (rows.hasNext()) {
                Row row = rows.next();
                
                // Bỏ qua các hàng không hợp lệ (nếu cần)
                if (row == null || row.getCell(0) == null || row.getCell(1) == null) {
                    continue; // Nếu không có dữ liệu, bỏ qua dòng này
                }
    
                Student student = new Student();
                
                // Kiểm tra ô và đảm bảo giá trị hợp lệ
                Cell studentCodeCell = row.getCell(0);
                Cell studentEmailCell = row.getCell(1);
    
                if (studentCodeCell != null && studentCodeCell.getCellType() == CellType.STRING) {
                    student.setStudentCode(studentCodeCell.getStringCellValue());
                } else {
                    continue; // Bỏ qua dòng nếu không có mã sinh viên
                }
    
                if (studentEmailCell != null && studentEmailCell.getCellType() == CellType.STRING) {
                    student.setStudentEmail(studentEmailCell.getStringCellValue());
                } else {
                    continue; // Bỏ qua dòng nếu không có email sinh viên
                }
    
                student.setStatus(true);
                student.setOrganization(organization);
                student.setExam(exam);
                students.add(student);
            }
        }
    
        return students;
    }    
}