package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CampusView {
    private long campusId;

    private String campusName;
}
