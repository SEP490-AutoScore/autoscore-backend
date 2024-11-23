// package com.CodeEvalCrew.AutoScore.controllers;

// import com.CodeEvalCrew.AutoScore.services.ai_info_service.AIInfoService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import jakarta.validation.constraints.NotBlank;

// @RestController
// @RequestMapping("/api/ai-info")
// public class AIInfoController {

//     @Autowired
//     private AIInfoService aiInfoService;

//     @PutMapping("/update-api-key")
//     public ResponseEntity<String> updateAiApiKey(@RequestParam @NotBlank String apiKey) {
//         aiInfoService.updateAiApiKey(apiKey);
//         return new ResponseEntity<>("AI API Key updated successfully.", HttpStatus.OK);
//     }
// }
