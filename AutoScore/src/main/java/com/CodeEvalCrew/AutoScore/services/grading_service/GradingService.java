package com.CodeEvalCrew.AutoScore.services.grading_service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.CodeEvalCrew.AutoScore.mappers.GradingProcessMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Grading.GradingRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.GradingProcessView;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Organization_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Paper;
import com.CodeEvalCrew.AutoScore.models.Entity.GradingProcess;
import com.CodeEvalCrew.AutoScore.models.Entity.Organization;
import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamPaperRepository;
import com.CodeEvalCrew.AutoScore.repositories.grading_process_repository.GradingProcessRepository;
import com.CodeEvalCrew.AutoScore.repositories.source_repository.SourceDetailRepository;
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
                if(org.getType().equals(Organization_Enum.CAMPUS)) request.setOrganizationId(org.getOrganizationId());
            }
            

            // for (Long studentId : request.getListStudent()) {
            //     Source_Detail sourceDetail = sourceDetailRepository.;
            // }
            Specification<GradingProcess> spec = GradingSpecification.hasForeignKey(request.getExamPaperId(), "examPaper", "examPaperId");

            Optional<GradingProcess> optionalProcess = gradingProcessRepository.findOne(spec);
            GradingProcess process;
            if (optionalProcess.isEmpty()) {
                process = new GradingProcess(
                        null, "Starting", 0, request.getListStudent().size(), LocalDateTime.now(), LocalDateTime.now(), examPaper
                );
            } else {
                process = optionalProcess.get();
                process.setStatus("Stating");
                process.setSuccessProcess(0);
            }

            gradingProcessRepository.save(process);

            
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

            return result;
        } catch (Exception e) {
            throw e;
        }
    }
}
