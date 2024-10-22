package com.CodeEvalCrew.AutoScore.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.SonarQube.SonarQubeRunnerRequest;

@Service
public class ThirdPartyUtil {

    public int sonarQubeRunner(SonarQubeRunnerRequest request) throws Exception {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "sonar-scanner", // SonarScanner command
                    "-Dsonar.projectKey=" + request.getProjectKey(), // Set project key
                    "-Dsonar.sources=" + request.getSource(), // Path to project source
                    "-Dsonar.host.url=" + request.getHostURL(), // URL of SonarQube instance
                    "-Dsonar.login=" + request.getToken() // Authentication token
            );
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

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Print the response (which contains the analysis results)
            System.out.println("SonarQube analysis results: " + response.toString());

            return result;
        } catch (Exception e) {
            // e.printStackTrace();
            throw e;
        }

    }

}
