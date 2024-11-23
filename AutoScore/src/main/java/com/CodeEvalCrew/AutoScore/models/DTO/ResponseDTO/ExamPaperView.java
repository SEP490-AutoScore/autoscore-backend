package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import java.util.Set;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Semester.SemesterView;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Exam_Status_Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamPaperView {
    private Long examPaperId;
    private String examPaperCode;
    private Set<ImportantView> importants;
    private Boolean isUsed;
    private Exam_Status_Enum status;
    private String instruction;
    private int duration;
    private SubjectView subject;
    private SemesterView semester;
}
