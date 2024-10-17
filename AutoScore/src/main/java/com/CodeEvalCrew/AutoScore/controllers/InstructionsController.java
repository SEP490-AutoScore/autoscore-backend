package com.CodeEvalCrew.AutoScore.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Instructions.InstructionCreateRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Instructions.InstructionViewRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.InstructionView;
import com.CodeEvalCrew.AutoScore.services.instruction_service.IIntructionService;

@RestController
@RequestMapping("/api/instructions")
public class InstructionsController {

    @Autowired
    private final IIntructionService instructionService;

    public InstructionsController(IIntructionService intructionService) {
        this.instructionService = intructionService;
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getById(@RequestParam Long id) {
        InstructionView result;
        try {

            result = instructionService.getById(id);

            return new ResponseEntity<>(result, HttpStatus.OK);
        // } catch (NotFoundException ex) {
        //     return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("list")
    public ResponseEntity<?> getList(@RequestBody InstructionViewRequest request) {
        List<InstructionView> result;
        try {
            result = instructionService.getList(request);
            return new ResponseEntity<>(result, HttpStatus.OK);
        // } catch (NotFoundException ex) {
        //     return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("")
    public ResponseEntity<?> createNewInstruction(@RequestBody InstructionCreateRequest request) {
        InstructionView result;
        try {
            result = instructionService.createNewInstruoction(request);
            return new ResponseEntity<>(result, HttpStatus.OK);
        // } catch (NotFoundException ex) {
        //     return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<?> updateInstruction(@PathVariable Long id, @RequestBody InstructionCreateRequest request) {
        InstructionView result;
        try {
            result = instructionService.updateInstruction(id, request);
            return new ResponseEntity<>(result, HttpStatus.OK);
        // } catch (NotFoundException ex) {
        //     return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
