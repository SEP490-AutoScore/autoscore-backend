package com.CodeEvalCrew.AutoScore.services.gherkin_scenario_service;

import org.springframework.stereotype.Service;

@Service
public class GherkinScenarioService implements IGherkinScenarioService {
    
}



// package com.CodeEvalCrew.AutoScore.services.testcase_service;

// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Paths;
// import java.util.Optional;

// import org.springframework.http.HttpEntity;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.HttpMethod;
// import org.springframework.http.ResponseEntity;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate;

// import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Database;
// import com.CodeEvalCrew.AutoScore.repositories.exam_repository.IExamBaremRepository;
// import com.CodeEvalCrew.AutoScore.repositories.examdatabase_repository.IExamDatabaseRepository;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.node.ObjectNode;

// @Service
// public class TestCaseService implements ITestCaseService {

//     private final RestTemplate restTemplate;
//     private final IExamDatabaseRepository examDatabaseRepository;
//     private final IExamBaremRepository examBaremRepository;

//     public TestCaseService(RestTemplate restTemplate, IExamDatabaseRepository examDatabaseRepository, IExamBaremRepository examBaremRepository) {
//         this.restTemplate = restTemplate;
//         this.examDatabaseRepository = examDatabaseRepository;
//         this.examBaremRepository = examBaremRepository;
//     }

//     @Override
//     public String getAIResponse(Long examDatabaseId) {
//         try {
//             // Retrieve the Exam_Database entity using the examDatabaseId
//             Optional<Exam_Database> examDatabaseOptional = examDatabaseRepository.findById(examDatabaseId);
//             if (!examDatabaseOptional.isPresent()) {
//                 return "Exam Database not found for ID: " + examDatabaseId;
//             }
//             Exam_Database examDatabase = examDatabaseOptional.get();
//             String databaseScript = examDatabase.getDatabaseScript();

//             // Create the first question
//             String question1 = "- Save the data below to your memory!! You don't need to explain anything!! \n- Below is the .sql file:\n" + databaseScript;
//             System.out.println(question1);
//             // Ask Gemini AI the first question
//             String response1 = sendToAI(question1);
//             System.out.println("Response to Question 1: " + response1);

//             // Read the contents of question2.txt
//             String question2 = new String(Files.readAllBytes(Paths.get("D:\\Desktop\\question2.txt")));
            
//             // Ask Gemini AI the second question
//             String response2 = sendToAI(question2);
//             System.out.println("Response to Question 2: " + response2);

//             return "Response to Question 1: " + response1 + "\nResponse to Question 2: " + response2;
//         } catch (IOException e) {
//             e.printStackTrace();
//             return "Failed to read question files.";
//         }
//     }

//     private String sendToAI(String prompt) {
//         // Set up the request to the AI service
//         String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=AIzaSyChK5Jo_vP3JM2xeCALY_QXLuCkoad-y5U";
//         HttpHeaders headers = new HttpHeaders();
//         headers.set("Content-Type", "application/json");

//         ObjectMapper objectMapper = new ObjectMapper();
//         ObjectNode contentNode = objectMapper.createObjectNode();
//         ObjectNode partsNode = objectMapper.createObjectNode();
//         partsNode.put("text", prompt);
//         contentNode.set("parts", objectMapper.createArrayNode().add(partsNode));
//         ObjectNode requestBodyNode = objectMapper.createObjectNode();
//         requestBodyNode.set("contents", objectMapper.createArrayNode().add(contentNode));

//         String requestBody;
//         try {
//             requestBody = objectMapper.writeValueAsString(requestBodyNode);
//         } catch (Exception e) {
//             throw new RuntimeException("Failed to construct AI request", e);
//         }

//         HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
//         ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);

//         // Log the raw response body for debugging
//         String responseBody = response.getBody();
//         System.out.println("Response Body: " + responseBody);

//         return responseBody;
//     }
// }
