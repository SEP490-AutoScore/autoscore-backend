package com.CodeEvalCrew.AutoScore.services.student_service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.CodeEvalCrew.AutoScore.models.Entity.Student;

@Service
public class ExcelService {

    public List<Student> importExcelFile(MultipartFile file) throws IOException {
        List<Student> students = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // Skip the header row
            if (rows.hasNext()) {
                rows.next();
            }

            while (rows.hasNext()) {
                Row row = rows.next();
                Student student = new Student();
                student.setStudentCode(row.getCell(0).getStringCellValue());
                student.setStudentEmail(row.getCell(1).getStringCellValue());
                student.setStatus(true);

                students.add(student);
            }
        }

        return students;
    }
}