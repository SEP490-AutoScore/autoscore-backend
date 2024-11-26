package com.CodeEvalCrew.AutoScore.services.score_service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.mappers.CodePlagiarismMapper;
import com.CodeEvalCrew.AutoScore.mappers.ScoreDetailMapper;
import com.CodeEvalCrew.AutoScore.mappers.ScoreMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.CodePlagiarismResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ScoreDetailsResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ScoreOverViewResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ScoreResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Code_Plagiarism;
import com.CodeEvalCrew.AutoScore.models.Entity.Score;
import com.CodeEvalCrew.AutoScore.models.Entity.Score_Detail;
import com.CodeEvalCrew.AutoScore.repositories.code_plagiarism_repository.CodePlagiarismRepository;
import com.CodeEvalCrew.AutoScore.repositories.score_detail_repository.ScoreDetailRepository;
import com.CodeEvalCrew.AutoScore.repositories.score_repository.ScoreRepository;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class ScoreService implements IScoreService {

    private final ScoreRepository scoreRepository;
    private final ScoreDetailRepository scoreDetailRepository;
    private final CodePlagiarismRepository codePlagiarismRepository;
    private final ScoreMapper scoreMapper;
    private final ScoreDetailMapper scoreDetailMapper;
    private final CodePlagiarismMapper codePlagiarismMapper;

    public ScoreService(ScoreRepository scoreRepository, ScoreDetailRepository scoreDetailRepository, CodePlagiarismRepository codePlagiarismRepository, ScoreMapper scoreMapper, ScoreDetailMapper scoreDetailMapper, CodePlagiarismMapper codePlagiarismMapper) {
        this.scoreRepository = scoreRepository;
        this.scoreDetailRepository = scoreDetailRepository;
        this.codePlagiarismRepository = codePlagiarismRepository;
        this.scoreMapper = scoreMapper;
        this.scoreDetailMapper = scoreDetailMapper;
        this.codePlagiarismMapper = codePlagiarismMapper;
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

    @Override
    public void exportScoresToExcel(HttpServletResponse response, List<ScoreResponseDTO> scores) throws IOException {
        if (scores == null || scores.isEmpty()) {
            throw new IllegalArgumentException("No scores available to export.");
        }
        String examPaperCode = scores.get(0).getExamPaperCode();
        String fileName = examPaperCode + "_score.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
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
            studentCodeCell2.setCellValue(scoreDTO.getPlagiarismReason());

            Cell plagiarismReasonCell2 = row.createCell(1);
            plagiarismReasonCell2.setCellValue(scoreDTO.getPlagiarismReason());

            // Cell codePlagiarismCell = row.createCell(2);
            // codePlagiarismCell.setCellValue(scoreDTO.getCodePlagiarism());
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

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public List<ScoreOverViewResponseDTO> getScoreOverView() {
        try {
            List<Long> examPaperIdList = scoreRepository.findDistinctExamPaperIds();
            if (examPaperIdList == null || examPaperIdList.isEmpty()) {
                return new ArrayList<>();
            }

            List<ScoreOverViewResponseDTO> responseDTOs = new ArrayList<>();

            for (Long examPaperId : examPaperIdList) {
                List<Score> scores = scoreRepository.findByExamPaperExamPaperId(examPaperId);
                if (scores == null || scores.isEmpty()) {
                    continue;
                }

                ScoreOverViewResponseDTO scoreOverViewResponseDTO = new ScoreOverViewResponseDTO();
                scoreOverViewResponseDTO.setExamPaperId(examPaperId);
                scoreOverViewResponseDTO.setExamCode(scores.get(0).getExamPaper().getExam().getExamCode());
                scoreOverViewResponseDTO.setExamPaperCode(scores.get(0).getExamPaper().getExamPaperCode());
                scoreOverViewResponseDTO.setSemesterName(scores.get(0).getExamPaper().getExam().getSemester().getSemesterName());
                scoreOverViewResponseDTO.setTotalStudents(scores.size());

                responseDTOs.add(scoreOverViewResponseDTO);
            }

            return responseDTOs;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<ScoreDetailsResponseDTO> getScoreDetailsByScoreId(Long scoreId) {
        try {
            List<Score_Detail> scoreDetails = scoreDetailRepository.findByScore_ScoreId(scoreId);

            if (scoreDetails != null) {
                return scoreDetailMapper.scoreDetailEntitiesToDTOs(scoreDetails);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<CodePlagiarismResponseDTO> getCodePlagiarismByScoreId(Long scoreId) {
        try {
            List<Code_Plagiarism> entities = codePlagiarismRepository.findByScoreScoreId(scoreId);
            return codePlagiarismMapper.toCodePlagiarismResponseDTOList(entities);
            // return codePlagiarismRepository.findByScoreScoreId(scoreId);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving plagiarism data for scoreId " + scoreId, e);
        }
    }
}
