package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import java.time.LocalDateTime;

import com.CodeEvalCrew.AutoScore.models.Entity.Exam;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamViewResponseDTO {
    private Long examId;
    private String examCode;
    private LocalDateTime examAt;
    private LocalDateTime gradingAt;
    private LocalDateTime publishAt;
    private String semesterName;
    private SubjectView subject;

    public ExamViewResponseDTO(Exam exam) {
        this.examId = exam.getExamId();
        this.examCode = exam.getExamCode();
        this.examAt = exam.getExamAt();
        this.gradingAt = exam.getCreatedAt();
        this.publishAt = exam.getPublishAt();
        this.semesterName = exam.getSemesterName();
        // this.campus = exam.getCampus();
        // this.subject = exam.getSubject();
    }

    public ExamViewResponseDTO() {
        super();
    }
}
