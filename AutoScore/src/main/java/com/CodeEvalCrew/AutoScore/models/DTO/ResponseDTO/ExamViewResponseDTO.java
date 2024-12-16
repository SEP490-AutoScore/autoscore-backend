package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import java.time.LocalDateTime;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Semester.SemesterView;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Exam_Type_Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExamViewResponseDTO {
    private Long examId;
    private String examCode;
    private LocalDateTime examAt;
    private LocalDateTime gradingAt;
    private LocalDateTime publishAt;
    private SemesterView semester;
    private SubjectView subject;
    private Exam_Type_Enum type;
    private boolean status;
}
