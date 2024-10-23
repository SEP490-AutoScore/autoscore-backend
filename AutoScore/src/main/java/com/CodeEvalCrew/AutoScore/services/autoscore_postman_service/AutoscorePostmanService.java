package com.CodeEvalCrew.AutoScore.services.autoscore_postman_service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.mappers.SourceDetailMapperforAutoscore;
import com.CodeEvalCrew.AutoScore.models.DTO.StudentSourceInfoDTO;
import com.CodeEvalCrew.AutoScore.repositories.source_repository.SourceRepository;
import com.CodeEvalCrew.AutoScore.services.autoscore_postman_service.Utils.AutoscoreInitUtils;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.api.model.Container;


@Service
public class AutoscorePostmanService implements IAutoscorePostmanService {

    @Autowired
    private SourceRepository sourceRepository;
    @Autowired
    private SourceDetailMapperforAutoscore sourceDetailMapper;

    @Override
    public List<StudentSourceInfoDTO> getStudentSourceInfoByExamPaperId(Long examPaperId, int numberOfAssignmentsDeployedAtTheSameTime) {
        List<StudentSourceInfoDTO> studentSources = sourceRepository.findByExamPaper_ExamPaperId(examPaperId)
            .map(source -> source.getSourceDetails()
                .stream()
                .map(sourceDetail -> sourceDetailMapper.toDTO(sourceDetail))
                .collect(Collectors.toList()))
            .orElseThrow(() -> new IllegalArgumentException("Exam Paper not found for ID: " + examPaperId));

        processStudentSolutions(studentSources);
        // Gọi deployAndScoring với số lượng bài triển khai đồng thời từ tham số
    deployAndScoring(studentSources, numberOfAssignmentsDeployedAtTheSameTime);

        return studentSources;
    }

    // Process each student solution and generate Docker files
    public void processStudentSolutions(List<StudentSourceInfoDTO> studentSources) {
        ExecutorService executor = Executors.newFixedThreadPool(studentSources.size());

        for (int i = 0; i < studentSources.size(); i++) {
            StudentSourceInfoDTO studentSource = studentSources.get(i);
            Path dirPath = Paths.get(studentSource.getStudentSourceCodePath());
            int port = AutoscoreInitUtils.BASE_PORT + i;
            Long studentId = studentSource.getStudentId();

            executor.submit(() -> {
                try {
                    AutoscoreInitUtils.removeDockerFiles(dirPath);
                    var csprojAndVersion = AutoscoreInitUtils.findCsprojAndDotnetVersion(dirPath);

                    if (csprojAndVersion != null) {
                        AutoscoreInitUtils.createDockerfile(dirPath, csprojAndVersion.getKey(), csprojAndVersion.getValue(), port);
                        AutoscoreInitUtils.createDockerCompose(dirPath, studentId, port);
                        AutoscoreInitUtils.findAndUpdateAppsettingsInBE(dirPath);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(60, TimeUnit.MINUTES);  // Đợi cho đến khi tất cả các nhiệm vụ hoàn thành
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }




    public void deployAndScoring(List<StudentSourceInfoDTO> studentSources, int numberOfAssignmentsDeployedAtTheSameTime) {
        int totalAssignments = studentSources.size();
        int deployed = 0;
    
        // Step 1: Multi-threaded deployment
        while (deployed < totalAssignments) {
            int end = Math.min(deployed + numberOfAssignmentsDeployedAtTheSameTime, totalAssignments);
            List<StudentSourceInfoDTO> batch = studentSources.subList(deployed, end);
            ExecutorService executor = Executors.newFixedThreadPool(numberOfAssignmentsDeployedAtTheSameTime);
    
            for (StudentSourceInfoDTO studentSource : batch) {
                executor.submit(() -> {
                    try {
                        // Deploy the student solution using multi-threading
                        deployStudentSolution(studentSource, numberOfAssignmentsDeployedAtTheSameTime);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
    
            // Wait for all tasks in the batch to complete
            executor.shutdown();
            try {
                executor.awaitTermination(60, TimeUnit.MINUTES);  // Wait until all tasks in the batch are completed
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    
            // Update the number of assignments that have been deployed
            deployed += numberOfAssignmentsDeployedAtTheSameTime;
        }
    
        // Step 2: Sequential scoring (single-threaded)
        for (StudentSourceInfoDTO studentSource : studentSources) {
            try {
                scoring(studentSource);  // Score each solution sequentially
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    
        // Step 3: Sequential container removal (single-threaded)
        for (StudentSourceInfoDTO studentSource : studentSources) {
            try {
                deleteContainer(studentSource);  // Remove each container sequentially
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    


    private void deployStudentSolution(StudentSourceInfoDTO studentSource, int numberOfAssignmentsDeployedAtTheSameTime) {
        System.out.println("Deploying solution for studentId: " + studentSource.getStudentId());
        Path dirPath = Paths.get(studentSource.getStudentSourceCodePath());
    
        try {
            // Start the `docker-compose up --build` process
            ProcessBuilder processBuilder = new ProcessBuilder("docker-compose", "up", "--build");
            processBuilder.directory(dirPath.toFile());
            processBuilder.inheritIO();
    
            Process process = processBuilder.start();
    
            // Periodically check if the number of running containers matches the expected count
            DockerClient dockerClient = DockerClientBuilder.getInstance("tcp://localhost:2375").build();
            boolean deploymentCompleted = false;
            int retries = 0;
            final int maxRetries = 10; // Maximum number of checks
            final int checkIntervalSeconds = 10; // Time between checks
    
            while (!deploymentCompleted && retries < maxRetries) {
                // Use Collections.singletonList to create a list containing the "running" status
                List<Container> runningContainers = dockerClient.listContainersCmd()
                        .withStatusFilter(Collections.singletonList("running"))
                        .exec();
                int runningCount = runningContainers.size();
    
                System.out.println("Checking running containers: " + runningCount + " out of " + numberOfAssignmentsDeployedAtTheSameTime);
    
                if (runningCount >= numberOfAssignmentsDeployedAtTheSameTime) {
                    deploymentCompleted = true;
                    System.out.println("Required number of containers are running.");
                } else {
                    // Wait before retrying
                    Thread.sleep(checkIntervalSeconds * 1000);
                    retries++;
                }
            }
    
            if (!deploymentCompleted) {
                System.err.println("Deployment did not complete within the expected time for studentId: " + studentSource.getStudentId());
            }
    
            // Close the Docker client connection
            dockerClient.close(); 
    
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    



    // Hàm chấm điểm
    private void scoring(StudentSourceInfoDTO studentSource) {
        System.out.println("Scoring solution for studentId: " + studentSource.getStudentId());
        // Thêm logic chấm điểm tại đây
    }

    
    // Hàm xóa tất cả container  Docker sau khi chấm điểm

 private void deleteContainer(StudentSourceInfoDTO studentSource) throws IOException {
    System.out.println("Deleting container for studentId: " + studentSource.getStudentId());

    DockerClient dockerClient = DockerClientBuilder.getInstance("tcp://localhost:2375").build();

    try {
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();

        for (Container container : containers) {
            System.out.println("Removing container " + container.getNames()[0] + " (" + container.getId().substring(0, 12) + ")");
            dockerClient.removeContainerCmd(container.getId()).withForce(true).exec();
        }

        System.out.println("All containers have been removed");

    } catch (DockerException e) {
        e.printStackTrace();
    } finally {
        dockerClient.close(); // Make sure to close the client
    }
}

}
