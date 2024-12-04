package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO;

import lombok.Data;

@Data
public class OrganizationRequestDTO {
    private Long organizationId;
    private String name;
    private String type;
    private Long parentId;
}
