package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CampusView {
    private Long campusId;
    private String campusName;
}
