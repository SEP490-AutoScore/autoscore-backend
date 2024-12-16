package com.CodeEvalCrew.AutoScore.services.student_service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam;
import com.CodeEvalCrew.AutoScore.models.Entity.Organization;
import com.CodeEvalCrew.AutoScore.models.Entity.Student;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamRepository;
import com.CodeEvalCrew.AutoScore.repositories.organization_repository.IOrganizationRepository;
import com.CodeEvalCrew.AutoScore.utils.UploadProgressListener;
import com.CodeEvalCrew.AutoScore.utils.Util;

@Service
public class ExcelService {

    private final IOrganizationRepository organizationRepoistory;
    private final IExamRepository examRepository;

    public ExcelService(IOrganizationRepository organizationRepoistory, IExamRepository examRepository) {
        this.organizationRepoistory = organizationRepoistory;
        this.examRepository = examRepository;
    }

    public List<Student> importExcelFile(MultipartFile file, Long examId, UploadProgressListener progressListener) throws IOException {
        List<Student> students = new ArrayList<>();
        long totalRows = calculateTotalRows(file);
        long processedRows = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // Bỏ qua dòng đầu tiên và dòng tiêu đề thừa
            if (rows.hasNext()) {
                rows.next(); // Dòng tiêu đề
                totalRows--;

            }
            if (rows.hasNext()) {
                rows.next(); // Dòng thừa "1 1 Student Code Email"
                totalRows--;
            }
            String orgName = Util.getCampus();
            Long organizationId = organizationRepoistory.findByName(orgName).get().getOrganizationId();

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

                processedRows++;
                progressListener.updateProgress(processedRows, totalRows);

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

    private long calculateTotalRows(MultipartFile file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            return sheet.getPhysicalNumberOfRows();
        }
    }
}
