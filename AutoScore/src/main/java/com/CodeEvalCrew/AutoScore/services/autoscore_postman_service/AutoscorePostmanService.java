package com.CodeEvalCrew.AutoScore.services.autoscore_postman_service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.mappers.SourceDetailMapperforAutoscore;
import com.CodeEvalCrew.AutoScore.models.DTO.StudentSourceInfoDTO;
import com.CodeEvalCrew.AutoScore.repositories.source_repository.SourceRepository;
import com.CodeEvalCrew.AutoScore.services.autoscore_postman_service.Utils.PostmanUtils;

@Service
public class AutoscorePostmanService implements IAutoscorePostmanService {

    @Autowired
    private SourceRepository sourceRepository;
    @Autowired
    private SourceDetailMapperforAutoscore sourceDetailMapper;

    @Override
    public List<StudentSourceInfoDTO> getStudentSourceInfoByExamPaperId(Long examPaperId) {
        List<StudentSourceInfoDTO> studentSources = sourceRepository.findByExamPaper_ExamPaperId(examPaperId)
            .map(source -> source.getSourceDetails()
                .stream()
                .map(sourceDetail -> sourceDetailMapper.toDTO(sourceDetail))
                .collect(Collectors.toList()))
            .orElseThrow(() -> new IllegalArgumentException("Exam Paper not found for ID: " + examPaperId));

        processStudentSolutions(studentSources);

        return studentSources;
    }

    // Process each student solution and generate Docker files
    public void processStudentSolutions(List<StudentSourceInfoDTO> studentSources) {
        ExecutorService executor = Executors.newFixedThreadPool(studentSources.size());

        for (int i = 0; i < studentSources.size(); i++) {
            StudentSourceInfoDTO studentSource = studentSources.get(i);
            Path dirPath = Paths.get(studentSource.getStudentSourceCodePath());
            int port = PostmanUtils.BASE_PORT + i;
            Long studentId = studentSource.getStudentId();

            executor.submit(() -> {
                try {
                    PostmanUtils.removeDockerFiles(dirPath);
                    var csprojAndVersion = PostmanUtils.findCsprojAndDotnetVersion(dirPath);

                    if (csprojAndVersion != null) {
                        PostmanUtils.createDockerfile(dirPath, csprojAndVersion.getKey(), csprojAndVersion.getValue(), port);
                        PostmanUtils.createDockerCompose(dirPath, studentId, port);
                        PostmanUtils.findAndUpdateAppsettingsInBE(dirPath);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
    }
}
