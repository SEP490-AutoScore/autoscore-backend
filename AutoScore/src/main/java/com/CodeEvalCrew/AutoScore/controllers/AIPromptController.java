package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.AIPrompt.CreatePromptDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AIPromptView;
import com.CodeEvalCrew.AutoScore.services.ai_prompt_service.IAIPromptService;


@RestController
@RequestMapping("api/ai-prompt/")
public class AIPromptController {
    @Autowired
    private final IAIPromptService aIPromptService;

    public AIPromptController(IAIPromptService aIPromptService) {
        this.aIPromptService = aIPromptService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPromptById(@RequestParam long id) {
        try{
            //call service
            AIPromptView result = aIPromptService.getPromptById(id);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }catch(Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("prompt/{id}")
    public ResponseEntity<?> getPromptOfUser(@RequestParam long id) {
        try{
            //call service
            List<AIPromptView> result = aIPromptService.getPromptByUser(id);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }catch(Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("create-prompt")
    public ResponseEntity<?> createNewPrompt(@RequestBody CreatePromptDTO request) {
        try{
            AIPromptView result;
            //call service
            result = aIPromptService.createNewPrompt(request);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }catch(Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
    }

    // @PutMapping("update-prompt")
    // public ResponseEntity<?> updateNewPrompt(@RequestBody CreatePromptDTO request) {
    //     try{
    //         AIPromptView result;
    //         //call service
            
    //         return new ResponseEntity<>(HttpStatus.OK);
    //     }catch(Exception e){
    //         return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    //     }
        
    // }

}
