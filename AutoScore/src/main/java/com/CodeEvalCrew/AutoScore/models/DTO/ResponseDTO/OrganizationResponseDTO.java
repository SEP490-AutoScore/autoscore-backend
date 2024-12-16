package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationResponseDTO {
    private Long organizationId;
    private String name;
    private String type;
    private boolean status;
    private Long parentId;
}
