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
@Data
@ToString
@Getter
@Setter
public class GradingRequestDTO {
    private List<Long> postmanForGradingIds;
    private Float scorePercentage;

}
