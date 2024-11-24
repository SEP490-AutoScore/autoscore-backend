package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostmanForGradingUpdateGetDTO {
    
    private String postmanFunctionName;
    private Float scoreOfFunction;
    private String fileCollectionPostman; 
}
