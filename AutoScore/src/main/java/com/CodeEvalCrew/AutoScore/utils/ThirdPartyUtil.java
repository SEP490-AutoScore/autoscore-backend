package com.CodeEvalCrew.AutoScore.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.SonarQube.SonarQubeRunnerRequest;

@Service
public class ThirdPartyUtil {

    public int sonarQubeRunner(@RequestBody SonarQubeRunnerRequest request) throws Exception {
        try {
            String sonarScannerPath = "C:\\SonarQube\\sonar-scanner-6.2.1.4610-windows-x64\\bin\\sonar-scanner.bat";


            ProcessBuilder processBuilder = new ProcessBuilder(
                sonarScannerPath,
                "-Dsonar.projectKey=VuongVT",
                "-Dsonar.sources=C:\\Project\\PE_PRN231_SU24_009909\\StudentSolution\\1\\vuongvtse160599\\0\\PEPRN231_SU24_009909_VoTrongVuong_BE",
                "-Dsonar.host.url=http://localhost:9000",
                "-Dsonar.scm.disabled=true",
                "-Dsonar.inclusions=**/*.cs",
                "-Dsonar.token=squ_d1b772d3f834297e3d5b47d80149ab45fc102408", // Use sonar.token instead of sonar.login
                "-Dsonar.projectBaseDir=C:\\Project\\PE_PRN231_SU24_009909\\StudentSolution\\1\\vuongvtse160599\\0\\PEPRN231_SU24_009909_VoTrongVuong_BE",
                "-Dsonar.language=cs" // Explicitly set the language to C#
            );


            // ProcessBuilder processBuilder = new ProcessBuilder(
            //         "sonar-scanner", // SonarScanner command
            //         "-Dsonar.projectKey=" + request.getProjectKey(), // Set project key
            //         "-Dsonar.sources=" + request.getSource(), // Path to project source
            //         "-Dsonar.host.url=" + request.getHostURL(), // URL of SonarQube instance
            //         "-Dsonar.login=" + request.getToken() // Authentication token
            // );

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("SonarQube analysis finished with exit code: " + exitCode);
            return exitCode;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public String sonarQubeResultFeatch(SonarQubeRunnerRequest request) throws Exception {
        String result = "";
        try {
            String uriString = request.getHostURL() + "/api/qualitygates/project_status?projectKey=" + request.getProjectKey();
            // URL to fetch the quality gate status of the project
            URI uri = new URI(uriString);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Basic " + request.getToken());  // Use Basic Auth if needed

            StringBuilder response;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }

            // Print the response (which contains the analysis results)
            System.out.println("SonarQube analysis results: " + response.toString());

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    public String deleteSonarQubeProject(String projectKey) throws Exception {
        String sonarQubeHost = "http://localhost:9000";  // SonarQube host URL
        String sonarToken = System.getenv("SONAR_TOKEN");  // Fetch token from environment
    
        if (sonarToken == null) {
            throw new IllegalStateException("SonarQube token is not set.");
        }
    
        String deleteUrl = sonarQubeHost + "/api/projects/delete?project=" + projectKey;
        URI i = new URI(deleteUrl);
        URL url = i.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Basic " + java.util.Base64.getEncoder().encodeToString((sonarToken + ":").getBytes()));
    
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
            return "Project with key '" + projectKey + "' was deleted successfully.";
        } else {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                throw new RuntimeException("Failed to delete project: " + response.toString());
            }
        }
    }

}
