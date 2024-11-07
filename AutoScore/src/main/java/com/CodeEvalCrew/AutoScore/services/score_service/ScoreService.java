package com.CodeEvalCrew.AutoScore.services.score_service;

import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
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
        // Set response headers for file download
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=scores.xlsx");

        // Create a new workbook and sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Scores");

        // Create header row
        Row headerRow = sheet.createRow(0);
        Cell studentCodeHeader = headerRow.createCell(0);
        studentCodeHeader.setCellValue("StudentCode");

        Cell scoreHeader = headerRow.createCell(1);
        scoreHeader.setCellValue("Score");

        // Populate data rows
        int rowNum = 1;
        for (ScoreResponseDTO scoreDTO : scores) {
            Row row = sheet.createRow(rowNum++);

            // Set StudentCode
            Cell studentCodeCell = row.createCell(0);
            studentCodeCell.setCellValue(scoreDTO.getStudentCode());

            // Set Score
            Cell scoreCell = row.createCell(1);
            scoreCell.setCellValue(scoreDTO.getTotalScore() != null ? scoreDTO.getTotalScore() : 0.0);
        }

        // Autosize columns for better readability
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);

        // Write the output to response output stream
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
