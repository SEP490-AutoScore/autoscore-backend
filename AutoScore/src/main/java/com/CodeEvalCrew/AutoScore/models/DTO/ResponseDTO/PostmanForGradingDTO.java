package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostmanForGradingDTO {
    private Long postmanForGradingId;
    private String postmanFunctionName;
    private Float scoreOfFunction;
    private Long totalPmTest;
    private Long orderBy;
    private Long postmanForGradingParentId; 
    private Long examQuestionId;           
    private Long gherkinScenarioId;        
}

// import lombok.AllArgsConstructor;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import lombok.Setter;

// @AllArgsConstructor
// @NoArgsConstructor
// @Getter
// @Setter
// public class PostmanForGradingDTO {
//     private Long postmanForGradingId;
//     private String postmanFunctionName;
//     private Long parentPostmanId; // should match with postmanForGradingParentId
//     private float totalPmtest; // note the type is float here
//     private float scoreOfFunction; // note the type is float here
// }



