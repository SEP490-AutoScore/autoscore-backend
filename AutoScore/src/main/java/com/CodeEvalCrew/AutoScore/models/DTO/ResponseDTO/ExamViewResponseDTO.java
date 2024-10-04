package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;

import com.CodeEvalCrew.AutoScore.models.Entity.Campus;
import com.CodeEvalCrew.AutoScore.models.Entity.Exam;
import com.CodeEvalCrew.AutoScore.models.Entity.Subject;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamViewResponseDTO {
    private long examId;

    private String examCode;

    private Timestamp examAt;

    private Timestamp gradingAt;

    private Timestamp publishAt;

    private String semesterName;

    private Campus campus;

    private Subject subject;

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
