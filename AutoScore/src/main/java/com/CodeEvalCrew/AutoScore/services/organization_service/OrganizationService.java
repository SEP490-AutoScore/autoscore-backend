package com.CodeEvalCrew.AutoScore.services.organization_service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.OrganizationRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OrganizationResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Organization_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Employee;
import com.CodeEvalCrew.AutoScore.models.Entity.Organization;
import com.CodeEvalCrew.AutoScore.repositories.account_organization_repository.AccountOrganizationRepository;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IEmployeeRepository;
import com.CodeEvalCrew.AutoScore.repositories.organization_repository.IOrganizationRepository;
import com.CodeEvalCrew.AutoScore.repositories.organization_repository.IOrganizationSubjectRepository;
import com.CodeEvalCrew.AutoScore.repositories.student_repository.StudentRepository;

import jakarta.transaction.Transactional;

@Service
public class OrganizationService implements IOrganizationService {

    private final IOrganizationRepository organizationRepository;
    private final IEmployeeRepository employeeRepository;
    private final IOrganizationSubjectRepository organizationSubjectRepository;
    private final AccountOrganizationRepository accountOrganizationRepository;
    private final StudentRepository studentRepository;

    public OrganizationService(IOrganizationRepository organizationRepository, IEmployeeRepository employeeRepository,
            IOrganizationSubjectRepository organizationSubjectRepository, AccountOrganizationRepository accountOrganizationRepository,
            StudentRepository studentRepository) {
        this.organizationRepository = organizationRepository;
        this.employeeRepository = employeeRepository;
        this.organizationSubjectRepository = organizationSubjectRepository;
        this.accountOrganizationRepository = accountOrganizationRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    public List<OrganizationResponseDTO> getAllOrganization() {
        try {
            List<Organization> organizations = organizationRepository.findAll();
            List<OrganizationResponseDTO> organizationResponseDTOs = new ArrayList<>();

            for (Organization organization : organizations) {
                OrganizationResponseDTO organizationResponseDTO = new OrganizationResponseDTO();
                organizationResponseDTO.setOrganizationId(organization.getOrganizationId());
                organizationResponseDTO.setName(organization.getName());
                organizationResponseDTO.setType(organization.getType().toString());
                organizationResponseDTO.setStatus(organization.isStatus());
                organizationResponseDTO.setParentId(organization.getParentId());
                organizationResponseDTOs.add(organizationResponseDTO);
            }

            return organizationResponseDTOs;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public OrganizationResponseDTO getOrganization(Long organizationId) {
        try {
            Optional<Organization> organization = organizationRepository.findById(organizationId);
            if (organization.isPresent()) {
                OrganizationResponseDTO organizationResponseDTO = new OrganizationResponseDTO();
                organizationResponseDTO.setOrganizationId(organization.get().getOrganizationId());
                organizationResponseDTO.setName(organization.get().getName());
                organizationResponseDTO.setType(organization.get().getType().toString());
                organizationResponseDTO.setStatus(organization.get().isStatus());
                organizationResponseDTO.setParentId(organization.get().getParentId());
                return organizationResponseDTO;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public OperationStatus createOrganization(OrganizationRequestDTO organizationRequestDTO) {
        try {
            Optional<List<Organization>> organizations = organizationRepository.findAllByName(organizationRequestDTO.getName());
            if (organizations.get() != null && !organizations.get().isEmpty()) {
                for (Organization organization : organizations.get()) {
                    if (organization.getName().equals(organizationRequestDTO.getName())
                            && Objects.equals(Organization_Enum.valueOf(organizationRequestDTO.getType().toUpperCase()), Organization_Enum.CAMPUS)) {
                        return OperationStatus.ALREADY_EXISTS;
                    }
                    if (Objects.equals(organization.getParentId(), organizationRequestDTO.getParentId())
                            && organization.getName().equals(organizationRequestDTO.getName())) {
                        return OperationStatus.ALREADY_EXISTS;
                    }
                }
            }

            Organization newOrganization = new Organization();
            newOrganization.setName(organizationRequestDTO.getName());
            newOrganization.setParentId(organizationRequestDTO.getParentId());
            newOrganization.setType(Organization_Enum.valueOf(organizationRequestDTO.getType().toUpperCase()));
            newOrganization.setStatus(true);

            Organization savedOrganization = organizationRepository.save(newOrganization);
            if (savedOrganization == null) {
                return OperationStatus.FAILURE;
            }

            return OperationStatus.SUCCESS;
        } catch (Exception e) {
            return OperationStatus.ERROR;
        }
    }

    @Override
    public OperationStatus updateOrganization(OrganizationRequestDTO organizationRequestDTO) {
        try {
            Optional<Organization> organizationRoot = organizationRepository.findById(organizationRequestDTO.getOrganizationId());
            if (!organizationRoot.isPresent()) {
                return OperationStatus.NOT_FOUND;
            }

            List<Organization> organizations = organizationRepository.findAll();
            for (Organization organization : organizations) {
                if (!Objects.equals(organization.getOrganizationId(), organizationRoot.get().getOrganizationId())
                        && organization.getName().equals(organizationRoot.get().getName())
                        && Objects.equals(Organization_Enum.valueOf(organizationRequestDTO.getType().toUpperCase()), Organization_Enum.CAMPUS)) {
                    return OperationStatus.ALREADY_EXISTS;
                }
                if (!Objects.equals(Organization_Enum.valueOf(organizationRequestDTO.getType().toUpperCase()), Organization_Enum.CAMPUS)
                        && Objects.equals(Organization_Enum.valueOf(organizationRequestDTO.getType().toUpperCase()), organizationRoot.get().getType())
                        && Objects.equals(organization.getParentId(), organizationRequestDTO.getParentId())
                        && organization.getName().equals(organizationRequestDTO.getName())
                        && !Objects.equals(organization.getOrganizationId(), organizationRequestDTO.getOrganizationId())) {
                    return OperationStatus.ALREADY_EXISTS;
                }
            }

            organizationRoot.get().setName(organizationRequestDTO.getName());
            Organization savedOrganization = organizationRepository.save(organizationRoot.get());
            if (savedOrganization == null) {
                return OperationStatus.FAILURE;
            }

            return OperationStatus.SUCCESS;
        } catch (Exception e) {
            return OperationStatus.ERROR;
        }
    }

    @Transactional
    @Override
    public OperationStatus deleteOrganization(Long organizationId) {
        try {
            // Kiểm tra tổ chức gốc
            Optional<Organization> organizationRoot = organizationRepository.findById(organizationId);
            if (!organizationRoot.isPresent()) {
                return OperationStatus.NOT_FOUND;
            }

            // Kiểm tra liên kết của tổ chức gốc
            if (hasDependencies(organizationId)) {
                return OperationStatus.CANNOT_DELETE;
            }

            // Lấy toàn bộ danh sách tổ chức con (bao gồm các cấp con, cháu...)
            List<Organization> allChildOrganizations = getAllChildOrganizations(organizationId);

            // Kiểm tra liên kết của tất cả các tổ chức con
            for (Organization child : allChildOrganizations) {
                if (hasDependencies(child.getOrganizationId())) {
                    return OperationStatus.ALREADY_EXISTS;
                }
            }

            // Xóa thủ công tất cả Employee liên kết với tổ chức
            Organization organization = organizationRoot.get();
            for (Employee employee : organization.getEmployees()) {
                employee.setOrganization(null); // Ngắt liên kết
                employeeRepository.save(employee);
            }

            // Xóa tất cả tổ chức con (theo thứ tự từ dưới lên)
            for (Organization child : allChildOrganizations) {
                organizationRepository.delete(child);
            }

            // Xóa tổ chức gốc
            organizationRepository.delete(organizationRoot.get());

            // Kiểm tra nếu tổ chức gốc vẫn tồn tại (xóa thất bại)
            if (organizationRepository.findById(organizationId).isPresent()) {
                return OperationStatus.FAILURE;
            }

            return OperationStatus.SUCCESS;
        } catch (Exception e) {
            // Xử lý lỗi chung
            return OperationStatus.ERROR;
        }
    }

    /**
     * Đệ quy lấy toàn bộ các tổ chức con ở mọi cấp
     */
    private List<Organization> getAllChildOrganizations(Long parentId) {
        List<Organization> allChildren = new ArrayList<>();
        List<Organization> directChildren = organizationRepository.findAllByParentId(parentId).orElse(new ArrayList<>());

        for (Organization child : directChildren) {
            allChildren.add(child);
            allChildren.addAll(getAllChildOrganizations(child.getOrganizationId())); // Đệ quy
        }

        return allChildren;
    }

    private boolean hasDependencies(Long organizationId) {
        // Kiểm tra liên kết với bảng khác
        return employeeRepository.existsByOrganizationOrganizationId(organizationId)
                || organizationSubjectRepository.existsByOrganizationOrganizationId(organizationId)
                || accountOrganizationRepository.existsByOrganizationOrganizationId(organizationId)
                || studentRepository.existsByOrganizationOrganizationId(organizationId);
    }
}
