package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO;

import java.util.List;

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
public class PostmanForGradingUpdateRequest {
    private Long examPaperId;
    private List<PostmanForGradingUpdateDTO> updateDTOs; 
}