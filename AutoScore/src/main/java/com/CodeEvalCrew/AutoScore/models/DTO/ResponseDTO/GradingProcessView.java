package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GradingProcessView {
    private Long processId;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime updateDate;
    private Long examPaperId;
}
