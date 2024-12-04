package com.CodeEvalCrew.AutoScore.services.organization_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.OrganizationRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OrganizationResponseDTO;

public interface IOrganizationService {
    List<OrganizationResponseDTO> getAllOrganization();
    public OrganizationResponseDTO getOrganization(Long organizationId);
    public OperationStatus createOrganization(OrganizationRequestDTO organizationRequestDTO);
    public OperationStatus updateOrganization(OrganizationRequestDTO organizationRequestDTO);
    public OperationStatus deleteOrganization(Long organizationId);
}
