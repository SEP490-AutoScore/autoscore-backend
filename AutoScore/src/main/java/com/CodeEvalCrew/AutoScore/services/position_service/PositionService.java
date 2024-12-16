package com.CodeEvalCrew.AutoScore.services.position_service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PositionRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PositionResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.models.Entity.Employee;
import com.CodeEvalCrew.AutoScore.models.Entity.Position;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IEmployeeRepository;
import com.CodeEvalCrew.AutoScore.repositories.position_repository.IPositionRepository;
import com.CodeEvalCrew.AutoScore.utils.Util;

@Service
public class PositionService implements IPositionService {

    private final IPositionRepository positionRepository;
    private final IEmployeeRepository employeeRepository;
    private final Util util;

    public PositionService(IPositionRepository positionRepository, IEmployeeRepository employeeRepository, Util util) {
        this.positionRepository = positionRepository;
        this.employeeRepository = employeeRepository;
        this.util = util;
    }

    @Override
    public List<PositionResponseDTO> getAllPosition() {
        try {
            List<Position> positions = positionRepository.findAll();
            List<PositionResponseDTO> positionResponseDTOs = new ArrayList<>();
            for (Position position : positions) {
                Optional<List<Employee>> employees = employeeRepository.findAllByPosition(position);
                PositionResponseDTO positionResponseDTO = new PositionResponseDTO();
                positionResponseDTO.setPositionId(position.getPositionId());
                positionResponseDTO.setName(position.getName());
                positionResponseDTO.setDescription(position.getDescription());
                positionResponseDTO.setStatus(position.isStatus());
                positionResponseDTO.setTotalUser(employees.get().size());
                positionResponseDTO.setLastUpdated(position.getLastUpdated());
                positionResponseDTOs.add(positionResponseDTO);
            }
            return positionResponseDTOs;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<PositionResponseDTO> getAllPositionByRole() {
        try {
            Account account = util.getAuthenticatedAccount();
            List<Position> positions = positionRepository.findAll();
            List<PositionResponseDTO> positionResponseDTOs = new ArrayList<>();

            String roleCode = account.getRole().getRoleCode();

            for (Position position : positions) {
                PositionResponseDTO positionResponseDTO = new PositionResponseDTO();
                boolean status = false;

                if ("ADMIN".equals(roleCode)) {
                    status = true;
                } else if ("EXAMINER".equals(roleCode) && !"ADMIN".equals(position.getPositionCode())) {
                    status = true;
                } else if ("HEAD_OF_DEPARTMENT".equals(roleCode)
                        && (!"ADMIN".equals(position.getPositionCode()) || !"EXAMINER".equals(position.getPositionCode()))) {
                    status = true;
                }

                positionResponseDTO.setStatus(status);
                positionResponseDTO.setPositionId(position.getPositionId());
                positionResponseDTO.setName(position.getName());
                positionResponseDTOs.add(positionResponseDTO);
            }

            return positionResponseDTOs;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public PositionResponseDTO getPositionById(Long positionId) {
        try {
            Optional<Position> position = positionRepository.findById(positionId);
            if (position.isEmpty()) {
                return null;
            }
            PositionResponseDTO positionResponseDTO = new PositionResponseDTO();
            positionResponseDTO.setPositionId(position.get().getPositionId());
            positionResponseDTO.setName(position.get().getName());
            positionResponseDTO.setDescription(position.get().getDescription());
            positionResponseDTO.setStatus(position.get().isStatus());
            positionResponseDTO.setLastUpdated(position.get().getLastUpdated());
            return positionResponseDTO;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public OperationStatus createPosition(PositionRequestDTO positionRequestDTO) {
        try {
            String name = positionRequestDTO.getName().trim();
            String description = positionRequestDTO.getDescription().trim();
            if (name == null || name.isEmpty() || description == null || description.isEmpty()) {
                return OperationStatus.INVALID_INPUT;
            }

            List<Position> positions = positionRepository.findAll();
            for (Position position : positions) {
                if (position.getName().equals(name)) {
                    return OperationStatus.ALREADY_EXISTS;
                }
            }

            Position position = new Position();
            position.setName(positionRequestDTO.getName().trim());
            position.setDescription(positionRequestDTO.getDescription().trim());
            position.setStatus(true);
            position.setLastUpdated(LocalDateTime.now());
            Position savedPosition = positionRepository.save(position);
            if (savedPosition == null) {
                return OperationStatus.FAILURE;
            }
            return OperationStatus.SUCCESS;
        } catch (Exception e) {
            return OperationStatus.ERROR;
        }
    }

    @Override
    public OperationStatus updatePosition(PositionRequestDTO positionRequestDTO) {
        try {
            Long positionId = positionRequestDTO.getPositionId();
            String name = positionRequestDTO.getName().trim();
            String description = positionRequestDTO.getDescription().trim();
            if (positionId == null || positionId <= 0 || name == null || name.isEmpty() || description == null || description.isEmpty()) {
                return OperationStatus.INVALID_INPUT;
            }

            Optional<Position> position = positionRepository.findById(positionId);
            if (position.isEmpty()) {
                return OperationStatus.NOT_FOUND;
            }

            Position existingPosition = position.get();
            if (!existingPosition.getName().equals(name)) {
                List<Position> positions = positionRepository.findAll();
                for (Position p : positions) {
                    if (!Objects.equals(p.getPositionId(), positionId) && p.getName().equals(name)) {
                        return OperationStatus.ALREADY_EXISTS;
                    }
                }
            }

            existingPosition.setName(name);
            existingPosition.setDescription(description);
            existingPosition.setLastUpdated(LocalDateTime.now());
            Position updatedPosition = positionRepository.save(existingPosition);
            if (updatedPosition == null) {
                return OperationStatus.FAILURE;
            }
            return OperationStatus.SUCCESS;
        } catch (Exception e) {
            return OperationStatus.ERROR;
        }
    }

    @Override
    public OperationStatus deletePosition(Long positionId) {
        try {
            Optional<Position> position = positionRepository.findById(positionId);
            if (position.isEmpty()) {
                return OperationStatus.NOT_FOUND;
            }

            if (positionRepository.existsByEmployeesPosition(position.get())) {
                return OperationStatus.CANNOT_DELETE;
            }

            positionRepository.deleteById(positionId);
            Optional<Position> deletedPosition = positionRepository.findById(positionId);
            if (deletedPosition.isPresent()) {
                return OperationStatus.FAILURE;
            }
            return OperationStatus.SUCCESS;
        } catch (Exception e) {
            return OperationStatus.ERROR;
        }
    }
}
