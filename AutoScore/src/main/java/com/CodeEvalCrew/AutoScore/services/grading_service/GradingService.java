package com.CodeEvalCrew.AutoScore.services.grading_service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.mappers.GradingProcessMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Grading.GradingRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Grading.GradingRequestForExam;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.GradingProcessView;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Exam_Status_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Exam_Type_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.GradingStatusEnum;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Organization_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.models.Entity.GradingProcess;
import com.CodeEvalCrew.AutoScore.models.Entity.Organization;
import com.CodeEvalCrew.AutoScore.models.Entity.Source;
import com.CodeEvalCrew.AutoScore.models.Entity.Source_Detail;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.grading_process_repository.GradingProcessRepository;
import com.CodeEvalCrew.AutoScore.repositories.source_repository.SourceDetailRepository;
import com.CodeEvalCrew.AutoScore.repositories.source_repository.SourceRepository;
import com.CodeEvalCrew.AutoScore.specification.GradingSpecification;
import com.CodeEvalCrew.AutoScore.utils.Util;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GradingService implements IGradingService {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private IExamPaperRepository examPaperRepository;
    @Autowired
    private GradingProcessRepository gradingProcessRepository;
    @Autowired
    private SourceDetailRepository sourceDetailRepository;
    @Autowired
    private SourceRepository sourceRepository;

    @Override
    public void startingGradingProcess(GradingRequest request) throws Exception, NotFoundException {
        try {
            //valide rq
            Exam_Paper examPaper = examPaperRepository.findById(request.getExamPaperId()).get();
            if (examPaper == null) {
                throw new NotFoundException("Exam Paper not found");
            }

            Set<Organization> orgs = Util.getOrganizations();
            for (Organization org : orgs) {
                if (org.getType().equals(Organization_Enum.CAMPUS)) {
                    request.setOrganizationId(org.getOrganizationId());
                }
            }
            // Tạo HttpClient
            HttpClient client = HttpClient.newHttpClient();

            // Chuyển đối tượng request thành JSON
            String requestBody = objectMapper.writeValueAsString(request);

            // Tạo HttpRequest
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8081/api/grading")) // Đổi URL theo đúng endpoint của bạn
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Gửi request và chờ phản hồi
            client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());

        } catch (JsonProcessingException e) {
            throw e;
        }
    }

    @Override
    @SuppressWarnings("UseSpecificCatch")
    public GradingProcessView loadingGradingProgress(Long examPaperId) {
        GradingProcessView result;
        try {
            Specification<GradingProcess> spec = GradingSpecification.hasForeignKey(examPaperId, "examPaper", "examPaperId");

            Optional<GradingProcess> optionalProcess = gradingProcessRepository.findOne(spec);
            GradingProcess process;
            if (optionalProcess.isEmpty()) {
                throw new NoSuchElementException("Process not found");
            } else {
                process = optionalProcess.get();
            }
            result = GradingProcessMapper.INSTANCE.gradingProcessToView(process);
            result.setExamPaperId(examPaperId);
            return result;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void startingGradingProcessForExamPaper(GradingRequestForExam request) throws Exception, NotFoundException {
        //valide rq
        Exam_Paper examPaper = examPaperRepository.findById(request.getExamPaperId()).get();
        if (examPaper == null) {
            throw new NotFoundException("Exam Paper not found");
        }

        Set<Organization> orgs = Util.getOrganizations();
        for (Organization org : orgs) {
            if (org.getType().equals(Organization_Enum.CAMPUS)) {
                request.setOrganizationId(org.getOrganizationId());
            }
        }

        Long acc = Util.getAuthenticatedAccountId();

        Optional<Source> optSource = sourceRepository.findByExamPaper_ExamPaperId(request.getExamPaperId());
        if (!optSource.isPresent()) {
            throw new NoSuchElementException("source not found");
        }

        List<Source_Detail> sourceDetails = sourceDetailRepository.findBySource_SourceId(optSource.get().getSourceId());
        if (sourceDetails == null) {
            throw new NotFoundException("No source found");
        }
        List<Long> students = new ArrayList<>();

        for (Source_Detail s : sourceDetails) {
            students.add(s.getStudent().getStudentId());
        }

        Specification<GradingProcess> spec = GradingSpecification.hasForeignKey(request.getExamPaperId(), "examPaper", "examPaperId");
        Optional<GradingProcess> optionalProcess = gradingProcessRepository.findOne(spec);
        GradingProcess process;
        if (optionalProcess.isEmpty()) {
            process = new GradingProcess(
                    null, GradingStatusEnum.PENDING, LocalDateTime.now(), LocalDateTime.now(), students, Exam_Type_Enum.valueOf(request.getExamType()), request.getOrganizationId(), acc, examPaper
            );
        } else {
            process = optionalProcess.get();
            process.setStudentIds(students);
            process.setStatus(GradingStatusEnum.PENDING);
        }

        gradingProcessRepository.save(process);
        examPaper.setStatus(Exam_Status_Enum.GRADING);
        examPaperRepository.save(examPaper);

        List<GradingStatusEnum> statuses = Arrays.asList(
                GradingStatusEnum.IMPORTANT,
                GradingStatusEnum.GRADING,
                GradingStatusEnum.PLAGIARISM
        );

        boolean exists = gradingProcessRepository.existsByStatusIn(statuses);
        if (!exists) {
            GradingRequest giveRequest = new GradingRequest(students, request.getExamPaperId(), request.getExamType(), request.getOrganizationId(), request.getNumberDeploy(), 0l, 0l);

            HttpClient client = HttpClient.newHttpClient();

            // Chuyển đối tượng request thành JSON
            String requestBody = objectMapper.writeValueAsString(giveRequest);

            // Tạo HttpRequest
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8081/api/grading/v2")) // Đổi URL theo đúng endpoint của bạn
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Gửi request và chờ phản hồi
            client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
        }
    }
}
