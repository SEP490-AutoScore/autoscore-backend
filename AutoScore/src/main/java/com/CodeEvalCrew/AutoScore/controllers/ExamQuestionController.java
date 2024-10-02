package com.CodeEvalCrew.AutoScore.controllers;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam_Question;
import com.CodeEvalCrew.AutoScore.services.examquestion_service.IExamQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/examquestion")
public class ExamQuestionController {

    @Autowired
    private IExamQuestionService examQuestionService;

    @GetMapping("/exampaper/{examPaperId}")
    public ResponseEntity<List<Exam_Question>> getExamQuestionsByExamPaperId(@PathVariable long examPaperId) {
        List<Exam_Question> questions = examQuestionService.getExamQuestionsByExamPaperId(examPaperId);

        if (questions.isEmpty()) {
            // Return 404 Not Found if no questions are found
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Return 200 OK with the list of questions
        return new ResponseEntity<>(questions, HttpStatus.OK);
    }
}
