package com.CodeEvalCrew.AutoScore.models.Entity;

import java.time.LocalDateTime;
import java.util.List;

import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Exam_Type_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.GradingStatusEnum;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GradingProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long processId;
    @Enumerated(EnumType.STRING)
    private GradingStatusEnum status;
    private LocalDateTime startDate;
    private LocalDateTime updateDate;
    private List<Long> studentIds;
    private Exam_Type_Enum examType;
    private Long organizationId;
    private Long createBy;
    //n-1 exam
    @OneToOne
    @JoinColumn(name = "examPaperId", nullable = true)
    private Exam_Paper examPaper;
}
