package com.CodeEvalCrew.AutoScore.services.exam_paper_service;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.mappers.ExamPaperMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamPaper.ExamPaperCreateRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamPaper.ExamPaperViewRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.ExamPaperView;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.GherkinScenarioInfoDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Question;
import com.CodeEvalCrew.AutoScore.models.Entity.Gherkin_Scenario;
import com.CodeEvalCrew.AutoScore.models.Entity.Important;
import com.CodeEvalCrew.AutoScore.models.Entity.Important_Exam_Paper;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamRepository;
import com.CodeEvalCrew.AutoScore.repositories.important_repository.ImportantExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.important_repository.ImportantRepository;
import com.CodeEvalCrew.AutoScore.specification.ExamPaperSpecification;
import com.CodeEvalCrew.AutoScore.utils.Util;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

@Service
public class ExamPaperService implements IExamPaperService {
    @Autowired
    private final IExamPaperRepository examPaperRepository;
    @Autowired
    private final IExamRepository examRepository;
    @Autowired
    private final ImportantRepository importantRepository;
    @Autowired
    private final ImportantExamPaperRepository importantExamPaperRepository;
    @Autowired
    private final ObjectMapper objectMapper;

    public ExamPaperService(IExamPaperRepository examPaperRepository,
            IExamRepository examRepository,
            ImportantRepository importantRepository,
            ImportantExamPaperRepository importantExamPaperRepository,
            ObjectMapper objectMapper) {
        this.examPaperRepository = examPaperRepository;
        this.examRepository = examRepository;
        this.objectMapper = objectMapper;
        this.importantRepository = importantRepository;
        this.importantExamPaperRepository = importantExamPaperRepository;
    }

    @Override
    public ExamPaperView getById(Long id) throws NotFoundException {
        try {

            Exam_Paper examPaper = checkEntityExistence(examPaperRepository.findById(id), "Exam Paper", id);

            return ExamPaperMapper.INSTANCE.examPAperToView(examPaper);
        } catch (NotFoundException nfe) {
            throw nfe;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    @Override
    public List<ExamPaperView> getList(ExamPaperViewRequest request) throws NotFoundException, NoSuchElementException {
        List<ExamPaperView> result = new ArrayList<>();
        try {
            // check exam
            checkEntityExistence(examRepository.findById(request.getExamId()), "Exam", request.getExamId());

            // crete spec
            Specification<Exam_Paper> spec = ExamPaperSpecification.hasForeignKey(request.getExamId(), "exam",
                    "examId");
            spec.and(ExamPaperSpecification.hasTrueStatus());

            List<Exam_Paper> listEntities = examPaperRepository.findAll(spec);

            if (listEntities.isEmpty())
                throw new NoSuchElementException("No exam paper found");

            for (Exam_Paper exam_Paper : listEntities) {
                result.add(ExamPaperMapper.INSTANCE.examPAperToView(exam_Paper));
            }

            return result;
        } catch (NotFoundException | NoSuchElementException nfe) {
            throw nfe;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    @Override
    @Transactional
    public ExamPaperView createNewExamPaper(ExamPaperCreateRequest request) throws NotFoundException {
        try {
            // check Exam
            Exam exam = checkEntityExistence(examRepository.findById(request.getExamId()), "Exam", request.getExamId());

            // set to add
            Set<Important_Exam_Paper> importants = new HashSet<>();

            // mapping
            Exam_Paper examPaper = ExamPaperMapper.INSTANCE.requestToExamPaper(request);

            // check important to add to exam paper
            for (Long importantId : request.getImportantIdList()) {
                Important important = checkEntityExistence(importantRepository.findById(importantId), "Important",
                        importantId);

                Important_Exam_Paper importantExamPaper = new Important_Exam_Paper(null, important, examPaper);

                importants.add(importantExamPaper);
            }

            // update side in4
            examPaper.setExam(exam);
            examPaper.setImportants(importants);
            examPaper.setStatus(true);
            examPaper.setCreatedAt(Util.getCurrentDateTime());
            examPaper.setCreatedBy(Util.getAuthenticatedAccountId());

            examPaperRepository.save(examPaper);

            return ExamPaperMapper.INSTANCE.examPAperToView(examPaper);
        } catch (NotFoundException nfe) {
            throw nfe;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    @Override
    @Transactional
    public ExamPaperView updateExamPaper(Long id, ExamPaperCreateRequest request) throws NotFoundException {
        try {
            // check ExamPaper
            Exam_Paper examPaper = checkEntityExistence(examPaperRepository.findById(id), "Exam Paper", id);

            // check Exam
            Exam exam = checkEntityExistence(examRepository.findById(request.getExamId()), "Exam", request.getExamId());

            // update side in4
            examPaper.setExamPaperCode(request.getExamPaperCode());
            examPaper.setExam(exam);
            examPaper.setStatus(true);
            examPaper.setUpdatedAt(Util.getCurrentDateTime());
            examPaper.setUpdatedBy(Util.getAuthenticatedAccountId());

            examPaperRepository.save(examPaper);

            return ExamPaperMapper.INSTANCE.examPAperToView(examPaper);
        } catch (NotFoundException nfe) {
            throw nfe;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    @Override
    public ExamPaperView deleteExamPaper(Long id) throws NotFoundException {
        try {

            Exam_Paper examPaper = checkEntityExistence(examPaperRepository.findById(id), "Exam Paper", id);

            examPaper.setStatus(false);
            examPaper.setDeletedAt(Util.getCurrentDateTime());
            examPaper.setDeletedBy(Util.getAuthenticatedAccountId());

            return ExamPaperMapper.INSTANCE.examPAperToView(examPaper);
        } catch (NotFoundException nfe) {
            throw nfe;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    private <T> T checkEntityExistence(Optional<T> entity, String entityName, Long entityId) throws NotFoundException {
        return entity.orElseThrow(() -> new NotFoundException(entityName + " id: " + entityId + " not found"));
    }

    @Override
    public void importPostmanCollections(Long examPaperId, List<MultipartFile> files) throws Exception {
        try {
            Exam_Paper examPaper = examPaperRepository.findById(examPaperId)
                    .orElseThrow(() -> new NotFoundException("Exam Paper not found for ID: " + examPaperId));

            for (MultipartFile file : files) {
                byte[] fileData = file.getBytes();
                java.io.File tempFile = convertBytesToFile(fileData, file.getOriginalFilename());

                // Check if the file contains valid JSON
                if (!isValidJson(tempFile)) {
                    throw new Exception("File " + file.getOriginalFilename() + " contains invalid JSON.");
                }

                // Run Newman test
                boolean isNewmanSuccess = runNewmanTest(tempFile);

                // Regardless of Newman test result, save the exam paper
                examPaper.setFileCollectionPostman(fileData);
                examPaper.setIsComfirmFile(true);
                examPaperRepository.save(examPaper);

                // Handle Newman result
                if (!isNewmanSuccess) {
                    // Optionally, you can log the failure or take additional actions
                    System.out.println("Newman test failed for file: " + file.getOriginalFilename());
                }

                // Clean up the temporary file
                Files.deleteIfExists(tempFile.toPath());
            }
        } catch (NotFoundException e) {
            throw new Exception("Exam Paper with ID " + examPaperId + " not found.", e);
        } catch (Exception e) {
            // Handle other exceptions that may arise during processing
            throw new Exception("Failed to import files: " + e.getMessage(), e);
        }
    }

    private java.io.File convertBytesToFile(byte[] fileData, String fileName) throws Exception {
        java.io.File tempFile = new java.io.File(System.getProperty("java.io.tmpdir") + "\\" + fileName);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(fileData);
        }
        return tempFile;
    }

    private boolean isValidJson(java.io.File file) {
        try {
            objectMapper.readTree(file); // Try to parse the JSON
            return true; // JSON is valid
        } catch (Exception e) {
            return false; // JSON is invalid
        }
    }

    private boolean runNewmanTest(java.io.File file) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Users\\Admin\\AppData\\Roaming\\npm\\newman.cmd", "run", "\"" + file.getAbsolutePath() + "\"");
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            // Capture output for debugging
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                StringBuilder output = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                    System.out.println(line); // Log each line for debugging
                }
            }

            int exitCode = process.waitFor();
            return exitCode == 0; // Return true if successful
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Return false on error
        }
    }

    public List<Long> getExamQuestionIdsByExamPaperId(Long examPaperId) throws NotFoundException {
        Exam_Paper examPaper = checkEntityExistence(examPaperRepository.findById(examPaperId), "Exam Paper",
                examPaperId);

        // Extract question IDs from the associated Exam_Question set
        List<Long> questionIds = examPaper.getExamQuestions().stream()
                .map(Exam_Question::getExamQuestionId)
                .collect(Collectors.toList());

        return questionIds;
    }

    @Override
    public List<GherkinScenarioInfoDTO> getGherkinScenariosByExamPaperId(Long examPaperId) throws NotFoundException {
        Exam_Paper examPaper = examPaperRepository.findById(examPaperId)
                .orElseThrow(() -> new NotFoundException("Exam Paper không tồn tại"));

        List<GherkinScenarioInfoDTO> result = new ArrayList<>();
        examPaper.getExamQuestions().forEach(examQuestion -> {
            Set<Gherkin_Scenario> gherkinScenarios = examQuestion.getGherkinScenarios();
            gherkinScenarios.forEach(gherkinScenario -> {
                result.add(new GherkinScenarioInfoDTO(
                        examPaperId,
                        examQuestion.getExamQuestionId(),
                        gherkinScenario.getGherkinScenarioId()));
            });
        });

        return result;
    }

}
