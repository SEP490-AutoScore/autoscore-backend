package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.Grading;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GradingRequest {
    private List<Long> listStudent;
    private Long examPaperId;
    private String examType;
    private Long organizationId;
    private int numberDeploy;
    private Long memory_Megabyte;
    private Long processors;
}
