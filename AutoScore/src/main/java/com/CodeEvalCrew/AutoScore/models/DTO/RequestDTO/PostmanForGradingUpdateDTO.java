package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
@ToString
public class PostmanForGradingUpdateDTO {
    private Long postmanForGradingId;
    private String postmanFunctionName;
    private Float scoreOfFunction;
    private Long postmanForGradingParentId;
}