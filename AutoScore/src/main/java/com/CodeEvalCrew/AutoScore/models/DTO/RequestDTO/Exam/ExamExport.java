package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Exam;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.ExamQuestion.ExamQuestionExport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExamExport {
    private String ExamCode;
    private String ExamPaperCode;
    private String Semester;
    private String SubjectCode;
    private int duration;
    private String instructions;
    private String important;
    private String databaseDescpription;
    private String databaseName;
    private String databaseNote;
    private List<ExamQuestionExport> questions;
    private byte[] databaseImage;
}
