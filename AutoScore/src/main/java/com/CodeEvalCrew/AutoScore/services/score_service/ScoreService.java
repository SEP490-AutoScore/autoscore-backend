package com.CodeEvalCrew.AutoScore.services.score_service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.models.Entity.Account_Organization;
import com.CodeEvalCrew.AutoScore.models.Entity.Code_Plagiarism;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Organization_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.models.Entity.Organization;
import com.CodeEvalCrew.AutoScore.models.Entity.Score;
import com.CodeEvalCrew.AutoScore.models.Entity.Score_Detail;
import com.CodeEvalCrew.AutoScore.repositories.account_organization_repository.AccountOrganizationRepository;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;
import com.CodeEvalCrew.AutoScore.repositories.code_plagiarism_repository.CodePlagiarismRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.score_detail_repository.ScoreDetailRepository;
import com.CodeEvalCrew.AutoScore.repositories.score_repository.ScoreRepository;
import com.CodeEvalCrew.AutoScore.utils.Util;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class ScoreService implements IScoreService {

    @Autowired
    private final IAccountRepository accountRepository;
    private final ScoreRepository scoreRepository;
    private final ScoreDetailRepository scoreDetailRepository;
    private final CodePlagiarismRepository codePlagiarismRepository;
    private final ScoreMapper scoreMapper;
    private final ScoreDetailMapper scoreDetailMapper;
    private final CodePlagiarismMapper codePlagiarismMapper;
    @Autowired
    private final AccountOrganizationRepository accountOrganizationRepository;
    @Autowired
    private final IExamPaperRepository examPaperRepository;

    public ScoreService(ScoreRepository scoreRepository, ScoreDetailRepository scoreDetailRepository,
            CodePlagiarismRepository codePlagiarismRepository, ScoreMapper scoreMapper,
            ScoreDetailMapper scoreDetailMapper, CodePlagiarismMapper codePlagiarismMapper,
            AccountOrganizationRepository accountOrganizationRepository,
            IExamPaperRepository examPaperRepository,
            IAccountRepository accountRepository) {
        this.scoreRepository = scoreRepository;
        this.scoreDetailRepository = scoreDetailRepository;
        this.codePlagiarismRepository = codePlagiarismRepository;
        this.scoreMapper = scoreMapper;
        this.scoreDetailMapper = scoreDetailMapper;
        this.codePlagiarismMapper = codePlagiarismMapper;
        this.accountOrganizationRepository = accountOrganizationRepository;
        this.examPaperRepository = examPaperRepository;
        this.accountRepository = accountRepository;
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

    public void exportTxtFiles(HttpServletResponse response, Long examPaperId) {
        String baseFolder = "TxtLogScore/";
        String exportFolder = baseFolder + "exampapercode_" + examPaperId + "/";
        String zipFilePath = exportFolder + ".zip";
        try {
            File parentFolder = new File(baseFolder);
            if (!parentFolder.exists()) {
                parentFolder.mkdirs();
            }
            File exportFolderFile = new File(exportFolder);
            if (exportFolderFile.exists()) {
                deleteFolderRecursively(exportFolderFile);
            }
            exportFolderFile.mkdirs();

            List<ScoreResponseDTO> scores = getScoresByExamPaperId(examPaperId);
            if (scores == null || scores.isEmpty()) {
                throw new IllegalArgumentException("No scores available to export.");
            }

            String examPaperCode = scores.get(0).getExamPaperCode();
            String examPaperFolder = baseFolder + examPaperCode + "/";
            File examFolder = new File(examPaperFolder);
            if (!examFolder.exists()) {
                examFolder.mkdirs();
            }

            for (ScoreResponseDTO score : scores) {
                if ((score.getLogRunPostman() == null || score.getLogRunPostman().isEmpty())
                        && (score.getReason() == null || score.getReason().isEmpty())) {
                    continue;
                }
                String studentFolderPath = examPaperFolder + "studentcode_" + score.getStudentCode() + "/";
                File studentFolder = new File(studentFolderPath);
                studentFolder.mkdirs();

                if (score.getLogRunPostman() != null && !score.getLogRunPostman().isEmpty()) {
                    File logFile = new File(studentFolderPath + "log_run_postman.txt");
                    try (FileWriter logWriter = new FileWriter(logFile)) {
                        logWriter.write(score.getLogRunPostman());
                    }
                }

                if (score.getReason() != null && !score.getReason().isEmpty()) {
                    File reasonFile = new File(studentFolderPath + "reason.txt");
                    try (FileWriter reasonWriter = new FileWriter(reasonFile)) {
                        reasonWriter.write("Reason: " + score.getReason());
                    }
                }
            }

            zipFolder(Paths.get(examPaperFolder), Paths.get(zipFilePath));

            deleteFolderRecursively(examFolder);
            try (InputStream is = new FileInputStream(zipFilePath); OutputStream os = response.getOutputStream()) {
                response.setContentType("application/zip");
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + examPaperCode + ".zip");
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            }
            deleteFolderRecursively(new File(exportFolder));
            Files.deleteIfExists(Paths.get(zipFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteFolderRecursively(File folder) {
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteFolderRecursively(file);
                    } else {
                        file.delete();
                    }
                }
            }
            folder.delete();
        }
    }

    public void zipFolder(Path sourceFolderPath, Path zipPath) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            Files.walk(sourceFolderPath).filter(path -> !Files.isDirectory(path)).forEach(path -> {
                ZipEntry zipEntry = new ZipEntry(sourceFolderPath.relativize(path).toString());
                try {
                    zipOutputStream.putNextEntry(zipEntry);
                    Files.copy(path, zipOutputStream);
                    zipOutputStream.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public void exportScoresToExcel(HttpServletResponse response, Long examPaperId) throws IOException {
        List<ScoreResponseDTO> scores = getScoresByExamPaperId(examPaperId);
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
        // Sheet plagiarismSheet = workbook.createSheet("PlagiarismSheet");
        // Row headerRow2 = plagiarismSheet.createRow(0);

        // Cell plagiarismReasonHeader2 = headerRow2.createCell(0);
        // plagiarismReasonHeader2.setCellValue("Plagiarism Reason");
        // plagiarismReasonHeader2.setCellStyle(boldStyle);
        // Cell codePlagiarismHeader = headerRow2.createCell(1);
        // codePlagiarismHeader.setCellValue("Code Plagiarism");
        // codePlagiarismHeader.setCellStyle(boldStyle);

        // int rowNum2 = 1;
        // for (ScoreResponseDTO scoreDTO : scores) {
        //     if (scoreDTO.getLevelOfPlagiarism() == null || scoreDTO.getPlagiarismReason().equals("N/A")) {
        //         continue;
        //     }
        //     Row row = plagiarismSheet.createRow(rowNum2++);

        //     Cell studentCodeCell2 = row.createCell(0);
        //     studentCodeCell2.setCellValue(scoreDTO.getPlagiarismReason());

        //     Cell plagiarismReasonCell2 = row.createCell(1);
        //     plagiarismReasonCell2.setCellValue(scoreDTO.getPlagiarismReason());
        // }

        // Autosize columns for better readability
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
        // for (int i = 0; i < 2; i++) {
        //     plagiarismSheet.autoSizeColumn(i);
        // }
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
            if (scoreDetails != null && !scoreDetails.isEmpty()) {
                return scoreDetailMapper.scoreDetailEntitiesToDTOs(scoreDetails);
            }
            return new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<CodePlagiarismResponseDTO> getCodePlagiarismByScoreId(Long scoreId) {
        try {
            List<Code_Plagiarism> entities = codePlagiarismRepository.findByScoreScoreId(scoreId);
            return codePlagiarismMapper.toCodePlagiarismResponseDTOList(entities);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving plagiarism data for scoreId " + scoreId, e);
        }
    }

    @Override
    public int getTotalStudentsByExamPaperId(Long examPaperId) {
        return scoreRepository.countByExamPaperExamPaperId(examPaperId);
    }

    @Override
    public int getTotalStudentsWithZeroScore(Long examPaperId) {
        return scoreRepository.countByExamPaperExamPaperIdAndTotalScore(examPaperId, 0);
    }

    @Override
    public int getTotalStudentsWithScoreGreaterThanZero(Long examPaperId) {
        return scoreRepository.countByExamPaperIdAndTotalScoreGreaterThan(examPaperId, 0);
    }

    @Override
    public List<StudentScoreDTO> getStudentScoresByExamPaperId(Long examPaperId) {
        List<Score> allScores = scoreRepository.findAllByExamPaper_ExamPaperId(examPaperId);

        return allScores.stream()
                .map(score -> new StudentScoreDTO(
                score.getStudent().getStudentCode(),
                score.getTotalScore()))
                .collect(Collectors.toList());
    }

    private String checkCampusForAccount(Long accountId) {

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

        Account userAccount = accountRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new RuntimeException("User account not found."));
        String roleCode = userAccount.getRole().getRoleCode();

        List<Score> scores = scoreRepository.findAll();

        if ("EXAMINER".equals(roleCode)) {

            String userCampus = checkCampusForAccount(authenticatedUserId);

            if (userCampus == null) {
                throw new IllegalArgumentException("Authenticated user does not belong to any CAMPUS.");
            }

            return scores.stream()
                    .filter(score -> score.getStudent().isStatus()
                    && score.getExamPaper().getExam().getType().toString().equals("EXAM")
                    && score.getStudent().getOrganization().getName().equals(userCampus))
                    .collect(Collectors.groupingBy(Score::getTotalScore, Collectors.counting()));
        } else {
            return scores.stream()
                    .filter(score -> score.getStudent().isStatus()
                    && score.getExamPaper().getExam().getType().toString().equals("ASSIGNMENT")
                    && score.getExamPaper().getCreatedBy().equals(authenticatedUserId))
                    .collect(Collectors.groupingBy(Score::getTotalScore, Collectors.counting()));
        }
    }

    @Override
    public ScoreCategoryDTO getScoreCategories() {
        Long authenticatedUserId = Util.getAuthenticatedAccountId();

        Account userAccount = accountRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new RuntimeException("User account not found."));
        String roleCode = userAccount.getRole().getRoleCode();

        List<Score> scores = scoreRepository.findAll();

        if ("EXAMINER".equals(roleCode)) {
            String userCampus = checkCampusForAccount(authenticatedUserId);

            return categorizeScores(scores.stream()
                    .filter(score -> score.getStudent().isStatus()
                    && score.getExamPaper().getExam().getType().toString().equals("EXAM")
                    && score.getStudent().getOrganization().getName().equals(userCampus))
                    .toList());
        } else {
            return categorizeScores(scores.stream()
                    .filter(score -> score.getStudent().isStatus()
                    && score.getExamPaper().getExam().getType().toString().equals("ASSIGNMENT")
                    && score.getExamPaper().getCreatedBy().equals(authenticatedUserId))
                    .toList());
        }
    }

    // Helper function to classify points
    private ScoreCategoryDTO categorizeScores(List<Score> scores) {
        long excellent = scores.stream()
                .filter(score -> score.getTotalScore() >= 9 && score.getTotalScore() <= 10)
                .count();

        long good = scores.stream()
                .filter(score -> score.getTotalScore() >= 8 && score.getTotalScore() < 9)
                .count();

        long fair = scores.stream()
                .filter(score -> score.getTotalScore() >= 5 && score.getTotalScore() < 8)
                .count();

        long poor = scores.stream()
                .filter(score -> score.getTotalScore() >= 4 && score.getTotalScore() < 5)
                .count();

        long bad = scores.stream()
                .filter(score -> score.getTotalScore() >= 0 && score.getTotalScore() < 4)
                .count();

        return new ScoreCategoryDTO(excellent, good, fair, poor, bad);
    }

    @Override
    public List<TopStudentDTO> getTopStudents() {
        Long authenticatedUserId = Util.getAuthenticatedAccountId();

        Account userAccount = accountRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new RuntimeException("User account not found."));
        String roleCode = userAccount.getRole().getRoleCode();

        List<Score> scores = scoreRepository.findAll();

        if ("EXAMINER".equals(roleCode)) {

            String userCampus = checkCampusForAccount(authenticatedUserId);

            if (userCampus == null) {
                throw new IllegalArgumentException("Authenticated user does not belong to any CAMPUS.");
            }

            return scores.stream()
                    .filter(score -> score.getStudent().isStatus()
                    && score.getExamPaper().getExam().getType().toString().equals("EXAM")
                    && score.getStudent().getOrganization().getName().equals(userCampus))
                    .sorted((s1, s2) -> Float.compare(s2.getTotalScore(), s1.getTotalScore()))
                    .limit(20)
                    .map(score -> new TopStudentDTO(
                    score.getStudent().getStudentCode(),
                    score.getStudent().getStudentEmail(),
                    score.getTotalScore(),
                    score.getExamPaper().getExam().getExamCode()))
                    .collect(Collectors.toList());
        } else {
            return scores.stream()
                    .filter(score -> score.getStudent().isStatus()
                    && score.getExamPaper().getExam().getType().toString().equals("ASSIGNMENT")
                    && score.getExamPaper().getCreatedBy().equals(authenticatedUserId))
                    .sorted((s1, s2) -> Float.compare(s2.getTotalScore(), s1.getTotalScore()))
                    .limit(20)
                    .map(score -> new TopStudentDTO(
                    score.getStudent().getStudentCode(),
                    score.getStudent().getStudentEmail(),
                    score.getTotalScore(),
                    score.getExamPaper().getExam().getExamCode()))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<Map<String, Object>> analyzeLog() {
        Long authenticatedUserId = Util.getAuthenticatedAccountId();
        if (authenticatedUserId == null) {
            throw new IllegalStateException("Authenticated user ID is null.");
        }

        Account userAccount = accountRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new RuntimeException("User account not found."));
        String roleCode = userAccount.getRole().getRoleCode();

        String userCampus = checkCampusForAccount(authenticatedUserId);
        if (userCampus == null) {
            throw new IllegalStateException("User campus is null.");
        }

        List<Score> scores = scoreRepository.findAll();
        List<Score> filteredScores = scores.stream()
                .filter(score -> {
                    if ("EXAMINER".equals(roleCode)) {
                        return score.getStudent() != null
                                && score.getStudent().isStatus()
                                && score.getExamPaper().getExam().getType().toString().equals("EXAM")
                                && score.getStudent().getOrganization().getName().equals(userCampus);
                    } else {
                        return score.getStudent() != null
                                && score.getStudent().isStatus()
                                && score.getExamPaper().getExam().getType().toString().equals("ASSIGNMENT")
                                && score.getExamPaper().getCreatedBy().equals(authenticatedUserId);
                    }
                })
                .collect(Collectors.toList());

        List<String> logDataList = filteredScores.stream()
                .map(score -> {
                    String logRunPostman = score.getLogRunPostman();
                    return logRunPostman != null ? logRunPostman : "";
                })
                .collect(Collectors.toList());

        Set<String> uniqueFunctionsSet = new HashSet<>();
        for (Score score : filteredScores) {
            Long examPaperId = score.getExamPaper().getExamPaperId();
            String logData = score.getLogRunPostman();

            Pattern pattern = Pattern.compile("→\\s*(.+)");
            Matcher matcher = pattern.matcher(logData);

            while (matcher.find()) {
                String functionName = matcher.group(1).trim();
                String uniqueKey = examPaperId + ":" + functionName;
                uniqueFunctionsSet.add(uniqueKey);
            }
        }

        Map<String, Integer> functionCountMap = new HashMap<>();
        for (String uniqueKey : uniqueFunctionsSet) {
            String functionName = uniqueKey.split(":")[1];
            functionCountMap.put(functionName, functionCountMap.getOrDefault(functionName, 0) + 1);
        }

        List<Map<String, Object>> formattedData = functionCountMap.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> entryMap = new HashMap<>();
                    entryMap.put("function", entry.getKey());
                    entryMap.put("occurrences", entry.getValue());
                    return entryMap;
                })
                .collect(Collectors.toList());

        return formattedData;
    }

    @Override
    public Map<String, Integer> analyzeScoresFullyPassLogRunPostman(Long examPaperId) {
        List<Score_Detail> scoreDetails = scoreDetailRepository.findAllByScore_ExamPaper_ExamPaperId(examPaperId);

        if (scoreDetails == null || scoreDetails.isEmpty()) {
            throw new RuntimeException("No score details available for the given exam paper");
        }

        Map<String, Integer> functionPassCounts = new HashMap<>();

        for (Score_Detail detail : scoreDetails) {
            if (detail.getNoPmtestAchieve().equals(detail.getTotalPmtest())) {
                functionPassCounts.put(detail.getPostmanFunctionName(),
                        functionPassCounts.getOrDefault(detail.getPostmanFunctionName(), 0) + 1);
            }
        }

        return functionPassCounts;
    }

    @Override
    public Map<String, Integer> analyzeScoresFailedAllTests(Long examPaperId) {
        List<Score_Detail> scoreDetails = scoreDetailRepository.findAllByScore_ExamPaper_ExamPaperId(examPaperId);

        if (scoreDetails == null || scoreDetails.isEmpty()) {
            throw new RuntimeException("No score details available for the given exam paper");
        }

        Map<String, Integer> functionFailCounts = new HashMap<>();

        for (Score_Detail detail : scoreDetails) {
            if (detail.getNoPmtestAchieve() != null && detail.getNoPmtestAchieve() == 0) {
                functionFailCounts.put(detail.getPostmanFunctionName(),
                        functionFailCounts.getOrDefault(detail.getPostmanFunctionName(), 0) + 1);
            }
        }

        return functionFailCounts;
    }

    @Override
    public Map<String, Integer> analyzeScoresPartialPassLogRunPostman(Long examPaperId) {
        Exam_Paper examPaper = examPaperRepository.findById(examPaperId)
                .orElseThrow(() -> new RuntimeException("Exam paper not found"));

        Set<Score> scores = examPaper.getScores();
        if (scores == null || scores.isEmpty()) {
            throw new RuntimeException("No scores available for the given exam paper");
        }

        Map<String, Integer> partialPassCounts = new HashMap<>();

        for (Score score : scores) {
            String log = score.getLogRunPostman();
            if (log == null || log.isEmpty()) {
                continue;
            }
            Set<String> partiallyPassedFunctions = extractPartiallyPassedFunctions(log);

            for (String function : partiallyPassedFunctions) {
                partialPassCounts.put(function, partialPassCounts.getOrDefault(function, 0) + 1);
            }
        }

        return partialPassCounts;
    }

    private Set<String> extractPartiallyPassedFunctions(String log) {
        Set<String> partiallyPassedFunctions = new HashSet<>();

        Pattern functionPattern = Pattern.compile("→\\s*(.+)$", Pattern.MULTILINE);
        Matcher functionMatcher = functionPattern.matcher(log);

        while (functionMatcher.find()) {
            String functionName = functionMatcher.group(1).trim();

            String functionLog = extractLogForFunction(log, functionName);

            // Check if the function has at least one test case passed ("√")
            if (hasPartialPass(functionLog)) {
                partiallyPassedFunctions.add(functionName);
            }
        }

        return partiallyPassedFunctions;
    }

    private boolean hasPartialPass(String functionLog) {
        // Return true if at least one "√" exists
        return countOccurrences(functionLog, "√") > 0;
    }

    private long countOccurrences(String text, String target) {
        return Pattern.compile(Pattern.quote(target)).matcher(text).results().count();
    }

    private String extractLogForFunction(String log, String functionName) {
        Pattern functionBlockPattern = Pattern.compile(
                "→\\s*" + Pattern.quote(functionName) + "\\b(.+?)(?=→|$)", Pattern.DOTALL);
        Matcher matcher = functionBlockPattern.matcher(log);

        return matcher.find() ? matcher.group(1) : "";
    }

    @Override
    public Map<String, Map<String, Double>> getTotalRunAndAverageResponseTime(Long examPaperId) {
        Exam_Paper examPaper = examPaperRepository.findById(examPaperId)
                .orElseThrow(() -> new RuntimeException("Exam paper not found"));

        Set<Score> scores = examPaper.getScores();
        if (scores == null || scores.isEmpty()) {
            throw new RuntimeException("No scores available for the given exam paper");
        }

        Map<String, Map<String, Double>> studentTotalAndAverageResponseTimes = new HashMap<>();

        for (Score score : scores) {
            String log = score.getLogRunPostman();
            if (log == null || log.isEmpty()) {
                continue;
            }

            // Extract total run duration and average response time from the log
            Double totalRunDuration = extractTotalRunDurationFromLog(log);
            Double averageResponseTime = extractAverageResponseTimeFromLog(log);

            // Create an inner map for this student
            Map<String, Double> studentData = new HashMap<>();
            studentData.put("totalRunDuration", totalRunDuration);
            studentData.put("averageResponseTime", averageResponseTime);

            // Store the inner map in the outer map with the student code as key
            studentTotalAndAverageResponseTimes.put(score.getStudent().getStudentCode(), studentData);
        }

        return studentTotalAndAverageResponseTimes;
    }

    // Helper method to extract total run duration from the log
    private Double extractTotalRunDurationFromLog(String log) {

        Pattern totalDurationPattern = Pattern.compile("total run duration: ([0-9.]+)s");
        Matcher matcher = totalDurationPattern.matcher(log);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        return 0.0;
    }

    // Helper method to extract average response time from the log
    private Double extractAverageResponseTimeFromLog(String log) {

        Pattern averageResponsePattern = Pattern.compile("average response time: ([0-9]+)ms");
        Matcher matcher = averageResponsePattern.matcher(log);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1)) / 1000.0;
        }
        return 0.0;
    }

    @Override
    public List<Map<String, String>> getCodePlagiarismDetailsByExamPaperId(Long examPaperId) {
        Exam_Paper examPaper = examPaperRepository.findById(examPaperId)
                .orElseThrow(() -> new RuntimeException("Exam paper not found"));

        Set<Score> scores = examPaper.getScores();
        if (scores == null || scores.isEmpty()) {
            throw new RuntimeException("No scores available for the given exam paper");
        }

        List<Map<String, String>> plagiarismDetails = new ArrayList<>();

        // Loop through each score and its code plagiarisms
        for (Score score : scores) {
            Set<Code_Plagiarism> codePlagiarisms = score.getCodePlagiarisms();
            if (codePlagiarisms != null) {
                for (Code_Plagiarism codePlagiarism : codePlagiarisms) {
                    Map<String, String> plagiarismDetail = new HashMap<>();
                    plagiarismDetail.put("studentCodePlagiarism", codePlagiarism.getStudentCodePlagiarism());
                    plagiarismDetail.put("plagiarismPercentage", codePlagiarism.getPlagiarismPercentage());
                    plagiarismDetails.add(plagiarismDetail);
                }
            }
        }

        return plagiarismDetails;
    }
}
