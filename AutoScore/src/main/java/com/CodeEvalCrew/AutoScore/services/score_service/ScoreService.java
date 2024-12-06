package com.CodeEvalCrew.AutoScore.services.score_service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.mappers.CodePlagiarismMapper;
import com.CodeEvalCrew.AutoScore.mappers.ScoreDetailMapper;
import com.CodeEvalCrew.AutoScore.mappers.ScoreMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.CodePlagiarismResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ScoreCategoryDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ScoreDetailsResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ScoreOverViewResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ScoreResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.StudentScoreDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.TopStudentDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Account_Organization;
import com.CodeEvalCrew.AutoScore.models.Entity.Code_Plagiarism;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Organization_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Organization;
import com.CodeEvalCrew.AutoScore.models.Entity.Score;
import com.CodeEvalCrew.AutoScore.models.Entity.Score_Detail;
import com.CodeEvalCrew.AutoScore.repositories.account_organization_repository.AccountOrganizationRepository;
import com.CodeEvalCrew.AutoScore.repositories.code_plagiarism_repository.CodePlagiarismRepository;
import com.CodeEvalCrew.AutoScore.repositories.score_detail_repository.ScoreDetailRepository;
import com.CodeEvalCrew.AutoScore.repositories.score_repository.ScoreRepository;
import com.CodeEvalCrew.AutoScore.utils.Util;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class ScoreService implements IScoreService {

    private final ScoreRepository scoreRepository;
    private final ScoreDetailRepository scoreDetailRepository;
    private final CodePlagiarismRepository codePlagiarismRepository;
    private final ScoreMapper scoreMapper;
    private final ScoreDetailMapper scoreDetailMapper;
    private final CodePlagiarismMapper codePlagiarismMapper;
    @Autowired
    private final AccountOrganizationRepository accountOrganizationRepository;

    public ScoreService(ScoreRepository scoreRepository, ScoreDetailRepository scoreDetailRepository,
            CodePlagiarismRepository codePlagiarismRepository, ScoreMapper scoreMapper,
            ScoreDetailMapper scoreDetailMapper, CodePlagiarismMapper codePlagiarismMapper,
            AccountOrganizationRepository accountOrganizationRepository) {
        this.scoreRepository = scoreRepository;
        this.scoreDetailRepository = scoreDetailRepository;
        this.codePlagiarismRepository = codePlagiarismRepository;
        this.scoreMapper = scoreMapper;
        this.scoreDetailMapper = scoreDetailMapper;
        this.codePlagiarismMapper = codePlagiarismMapper;
        this.accountOrganizationRepository = accountOrganizationRepository;
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

            // row.setHeight((short) (sheet.getDefaultRowHeightInPoints() *
            // calculateRowHeight(scoreDTO)));
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
            // row.setHeight((short) (plagiarismSheet.getDefaultRowHeightInPoints() *
            // calculateRowHeight(scoreDTO)));
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
                scoreOverViewResponseDTO
                        .setSemesterName(scores.get(0).getExamPaper().getExam().getSemester().getSemesterName());
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

    @Override
    public int getTotalStudentsByExamPaperId(Long examPaperId) {
        // Sử dụng repository để đếm số lượng sinh viên có examPaperId
        return scoreRepository.countByExamPaperExamPaperId(examPaperId);
    }

    @Override
    public int getTotalStudentsWithZeroScore(Long examPaperId) {
        // Sử dụng repository để đếm số sinh viên có totalScore = 0
        return scoreRepository.countByExamPaperExamPaperIdAndTotalScore(examPaperId, 0);
    }

    @Override
    public int getTotalStudentsWithScoreGreaterThanZero(Long examPaperId) {
        // Sử dụng repository để đếm số sinh viên có totalScore > 0
        return scoreRepository.countByExamPaperIdAndTotalScoreGreaterThan(examPaperId, 0);
    }

    @Override
    public List<StudentScoreDTO> getStudentScoresByExamPaperId(Long examPaperId) {
        return scoreRepository.findStudentScoresByExamPaperId(examPaperId);
    }

    @Override
    public List<TopStudentDTO> getTopStudents() {

        Long authenticatedUserId = Util.getAuthenticatedAccountId();
        // Lấy campus của tài khoản hiện tại
        String userCampus = checkCampusForAccount(authenticatedUserId);

        // Truy vấn điểm của tất cả sinh viên
        List<Score> scores = scoreRepository.findAll(); // Sử dụng phương thức phù hợp của ScoreRepository

        // Lọc sinh viên có status=true, exam type=EXAM, campus trùng với tài khoản và
        // điểm cao nhất
        List<TopStudentDTO> topStudents = scores.stream()
                .filter(score -> score.getStudent().isStatus() &&
                        score.getExamPaper().getExam().getType().toString().equals("EXAM") &&
                        score.getStudent().getOrganization().getName().equals(userCampus))
                .sorted((s1, s2) -> Float.compare(s2.getTotalScore(), s1.getTotalScore())) // Sort by highest score
                .limit(20) // Get the top 20 students
                .map(score -> new TopStudentDTO(
                        score.getStudent().getStudentCode(),
                        score.getStudent().getStudentEmail(),
                        score.getTotalScore(),
                        score.getExamPaper().getExam().getExamCode())) // Access Exam through Exam_Paper
                .collect(Collectors.toList());

        return topStudents;
    }

    private String checkCampusForAccount(Long accountId) {
        // Hàm kiểm tra campus của tài khoản
        List<Account_Organization> accountOrganizations = accountOrganizationRepository
                .findByAccount_AccountId(accountId);

        for (Account_Organization accountOrg : accountOrganizations) {
            Organization organization = accountOrg.getOrganization();
            if (organization.getType() == Organization_Enum.CAMPUS) {
                return organization.getName();
            }
        }

        return null;
    }

    @Override
public Map<Float, Long> getTotalScoreOccurrences() {
    Long authenticatedUserId = Util.getAuthenticatedAccountId();
    String userCampus = checkCampusForAccount(authenticatedUserId);

    // Fetch all scores
    List<Score> scores = scoreRepository.findAll(); // Assuming findAll fetches all scores

    // Filter and group scores
    Map<Float, Long> scoreOccurrences = scores.stream()
            .filter(score -> score.getStudent().isStatus() && // Filter by active students
                    score.getExamPaper().getExam().getType().toString().equals("EXAM") && // Filter by exam type
                    score.getStudent().getOrganization().getName().equals(userCampus)) // Match campus
            .collect(Collectors.groupingBy(Score::getTotalScore, Collectors.counting())); // Group by totalScore

    return scoreOccurrences;
}

@Override
 public ScoreCategoryDTO getScoreCategories() {
        Long authenticatedUserId = Util.getAuthenticatedAccountId();
        String userCampus = checkCampusForAccount(authenticatedUserId);

        // Lấy tất cả các điểm
        List<Score> scores = scoreRepository.findAll();

        // Lọc và phân loại điểm
        long excellent = scores.stream()
                .filter(score -> score.getStudent().isStatus()
                        && score.getExamPaper().getExam().getType().toString().equals("EXAM")
                        && score.getStudent().getOrganization().getName().equals(userCampus)
                        && score.getTotalScore() >= 9 && score.getTotalScore() <= 10)
                .count();

        long good = scores.stream()
                .filter(score -> score.getStudent().isStatus()
                        && score.getExamPaper().getExam().getType().toString().equals("EXAM")
                        && score.getStudent().getOrganization().getName().equals(userCampus)
                        && score.getTotalScore() >= 8 && score.getTotalScore() < 9)
                .count();

        long fair = scores.stream()
                .filter(score -> score.getStudent().isStatus()
                        && score.getExamPaper().getExam().getType().toString().equals("EXAM")
                        && score.getStudent().getOrganization().getName().equals(userCampus)
                        && score.getTotalScore() >= 5 && score.getTotalScore() < 8)
                .count();

        long poor = scores.stream()
                .filter(score -> score.getStudent().isStatus()
                        && score.getExamPaper().getExam().getType().toString().equals("EXAM")
                        && score.getStudent().getOrganization().getName().equals(userCampus)
                        && score.getTotalScore() >= 4 && score.getTotalScore() < 5)
                .count();

        long bad = scores.stream()
                .filter(score -> score.getStudent().isStatus()
                        && score.getExamPaper().getExam().getType().toString().equals("EXAM")
                        && score.getStudent().getOrganization().getName().equals(userCampus)
                        && score.getTotalScore() >= 0 && score.getTotalScore() < 4)
                .count();

        return new ScoreCategoryDTO(excellent, good, fair, poor, bad);
    }

    @Override
    public List<Map<String, Object>> analyzeLog() {
        Long authenticatedUserId = Util.getAuthenticatedAccountId();  // Lấy ID người dùng đã xác thực
        String userCampus = checkCampusForAccount(authenticatedUserId); // Kiểm tra campus của tài khoản người dùng
    
        // Lấy tất cả các điểm số
        List<Score> scores = scoreRepository.findAll();
    
        // Lọc các điểm số theo điều kiện tương tự như trong getTotalScoreOccurrences
        List<String> logDataList = scores.stream()
                .filter(score -> score.getStudent().isStatus() && // Lọc học sinh đang hoạt động
                        score.getExamPaper().getExam().getType().toString().equals("EXAM") && // Lọc theo loại bài thi
                        score.getStudent().getOrganization().getName().equals(userCampus)) // Lọc theo campus
                .map(score -> score.getLogRunPostman())  // Lấy logRunPostman từ mỗi đối tượng Score
                .collect(Collectors.toList());
    
        // Tạo một Map để chứa tên hàm và số lần xuất hiện
        Map<String, Integer> functionCountMap = new HashMap<>();
    
        // Duyệt qua tất cả các logData và phân tích
        for (String logData : logDataList) {
            // Biểu thức chính quy để tìm các hàm sau dấu '→'
            Pattern pattern = Pattern.compile("→\\s*([a-zA-Z0-9_]+)");
            Matcher matcher = pattern.matcher(logData);
    
            // Duyệt qua tất cả các khớp với mẫu regex
            while (matcher.find()) {
                // Lấy tên hàm từ nhóm tìm được
                String functionName = matcher.group(1);
    
                // Tăng số lần xuất hiện của hàm đó
                functionCountMap.put(functionName, functionCountMap.getOrDefault(functionName, 0) + 1);
            }
        }
    
        // Chuyển đổi Map thành List với cấu trúc dữ liệu cần thiết cho biểu đồ
        List<Map<String, Object>> formattedData = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : functionCountMap.entrySet()) {
            Map<String, Object> entryMap = new HashMap<>();
            entryMap.put("function", entry.getKey());
            entryMap.put("occurrences", entry.getValue());
            formattedData.add(entryMap);
        }
    
        return formattedData;
    }
    



    

}
