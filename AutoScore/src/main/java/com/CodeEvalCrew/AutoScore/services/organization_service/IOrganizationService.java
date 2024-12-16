package com.CodeEvalCrew.AutoScore.services.organization_service;

import java.util.List;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.OrganizationRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OrganizationResponseDTO;

public interface IOrganizationService {
    List<OrganizationResponseDTO> getAllOrganization();
    OrganizationResponseDTO getOrganization(Long organizationId);
    OperationStatus createOrganization(OrganizationRequestDTO organizationRequestDTO);
    OperationStatus updateOrganization(OrganizationRequestDTO organizationRequestDTO);
    OperationStatus deleteOrganization(Long organizationId);
    List<OrganizationResponseDTO> getAllOrganizationByRole();
}
