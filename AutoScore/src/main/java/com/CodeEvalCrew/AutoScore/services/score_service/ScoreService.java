package com.CodeEvalCrew.AutoScore.services.score_service;

import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.mappers.ScoreMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ScoreResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Score;
import com.CodeEvalCrew.AutoScore.repositories.score_repository.ScoreRepository;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class ScoreService implements IScoreService {

    private final ScoreRepository scoreRepository;
    private final ScoreMapper scoreMapper;

    public ScoreService(ScoreRepository scoreRepository, ScoreMapper scoreMapper) {
        this.scoreRepository = scoreRepository;
        this.scoreMapper = scoreMapper;
    }

    @Override
    public List<ScoreResponseDTO> getScoresByExamPaperId(Long examPaperId) {
        try {
            List<Score> scores = scoreRepository.findByExamPaperExamPaperId(examPaperId);
            if (scores != null) {
                return scoreMapper.scoreEntityToScoreResponseDTO(scores);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public void exportScoresToExcel(HttpServletResponse response, List<ScoreResponseDTO> scores) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=scores.xlsx");
        Workbook workbook = new XSSFWorkbook();

        CellStyle boldStyle = workbook.createCellStyle();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        boldStyle.setFont(boldFont);

        // --- Sheet 2---
        Sheet sheet = workbook.createSheet("ScoresSheet");
        Row headerRow = sheet.createRow(0);

        Cell studentCodeHeader = headerRow.createCell(0);
        studentCodeHeader.setCellValue("Student Code");
        studentCodeHeader.setCellStyle(boldStyle);

        Cell scoreHeader = headerRow.createCell(1);
        scoreHeader.setCellValue("Score");
        scoreHeader.setCellStyle(boldStyle);

        Cell levelOfPlagiarismHeader = headerRow.createCell(2);
        levelOfPlagiarismHeader.setCellValue("Level Of Plagiarism");
        levelOfPlagiarismHeader.setCellStyle(boldStyle);

        Cell plagiarismReasonHeader = headerRow.createCell(3);
        plagiarismReasonHeader.setCellValue("Plagiarism Reason");
        plagiarismReasonHeader.setCellStyle(boldStyle);

        int rowNum = 1;
        for (ScoreResponseDTO scoreDTO : scores) {
            Row row = sheet.createRow(rowNum++);

            Cell studentCodeCell = row.createCell(0);
            studentCodeCell.setCellValue(scoreDTO.getStudentCode());

            Cell scoreCell = row.createCell(1);
            scoreCell.setCellValue(scoreDTO.getTotalScore() != null ? scoreDTO.getTotalScore() : 0.0);

            Cell levelOfPlagiarismCell = row.createCell(2);
            levelOfPlagiarismCell.setCellValue(scoreDTO.getLevelOfPlagiarism());

            Cell plagiarismReasonCell = row.createCell(3);
            plagiarismReasonCell.setCellValue(scoreDTO.getPlagiarismReason());

            // row.setHeight((short) (sheet.getDefaultRowHeightInPoints() * calculateRowHeight(scoreDTO)));
        }

        // --- Sheet 2---
        Sheet plagiarismSheet = workbook.createSheet("PlagiarismSheet");
        Row headerRow2 = plagiarismSheet.createRow(0);

        Cell plagiarismReasonHeader2 = headerRow2.createCell(0);
        plagiarismReasonHeader2.setCellValue("Plagiarism Reason");
        plagiarismReasonHeader2.setCellStyle(boldStyle);
        Cell codePlagiarismHeader = headerRow2.createCell(1);
        codePlagiarismHeader.setCellValue("Code Plagiarism");
        codePlagiarismHeader.setCellStyle(boldStyle);

        int rowNum2 = 1;
        for (ScoreResponseDTO scoreDTO : scores) {
            if (scoreDTO.getLevelOfPlagiarism() == null || scoreDTO.getPlagiarismReason().equals("N/A")) {
                continue;
            }
            Row row = plagiarismSheet.createRow(rowNum2++);

            Cell studentCodeCell2 = row.createCell(0);
            studentCodeCell2.setCellValue(scoreDTO.getStudentCode());

            Cell plagiarismReasonCell2 = row.createCell(1);
            plagiarismReasonCell2.setCellValue(scoreDTO.getPlagiarismReason());

            Cell codePlagiarismCell = row.createCell(2);
            codePlagiarismCell.setCellValue(scoreDTO.getCodePlagiarism());
            // row.setHeight((short) (plagiarismSheet.getDefaultRowHeightInPoints() * calculateRowHeight(scoreDTO)));
        }
        // Autosize columns for better readability
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
        for (int i = 0; i < 2; i++) {
            plagiarismSheet.autoSizeColumn(i);
        }
        // Write the output to response output stream
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // private int calculateRowHeight(ScoreResponseDTO scoreDTO) {
    //     // Hàm để tính toán chiều cao của hàng dựa trên nội dung 
    //     // Ví dụ đơn giản: trả về 2 nếu có nhiều hơn một dòng text trong bất kỳ field nào 
    //     int lines = 1;
    //     if (scoreDTO.getStudentCode() != null && scoreDTO.getStudentCode().contains("\n")) {
    //         lines++;
    //     }
    //     if (scoreDTO.getTotalScore() != null && String.valueOf(scoreDTO.getTotalScore()).contains("\n")) {
    //         lines++;
    //     }
    //     if (scoreDTO.getLevelOfPlagiarism() != null && scoreDTO.getLevelOfPlagiarism().contains("\n")) {
    //         lines++;
    //     }
    //     if (scoreDTO.getPlagiarismReason() != null && scoreDTO.getPlagiarismReason().contains("\n")) {
    //         lines++;
    //     }
    //     if (scoreDTO.getCodePlagiarism() != null && scoreDTO.getCodePlagiarism().contains("\n")) {
    //         lines++;
    //     }
    //     return lines;
    // }
}
