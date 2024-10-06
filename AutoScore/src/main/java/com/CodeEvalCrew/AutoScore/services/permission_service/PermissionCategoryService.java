package com.CodeEvalCrew.AutoScore.services.permission_service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.exceptions.Exception;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.PermissionCategoryRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.OperationStatus;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.PermissionCategoryDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Permission_Category;
import com.CodeEvalCrew.AutoScore.repositories.permission_repository.IPermissionCategoryRepository;

@Service
public class PermissionCategoryService implements IPermisionCategoryService {

    @Autowired
    private IPermissionCategoryRepository permissionCategoryRepository;

    @Override
    public List<PermissionCategoryDTO> getAllPermissionCategory() {
        try {
            // Lấy danh sách từ cơ sở dữ liệu
            List<Permission_Category> permissionCategories = permissionCategoryRepository.findAll();

            // Chuyển đổi từ Entity sang DTO
            List<PermissionCategoryDTO> permissionCategoryDTOs = permissionCategories.stream()
                    .map(permissionCategory -> new PermissionCategoryDTO(permissionCategory.getPermissionCategoryId(),
                    permissionCategory.getPermissionCategoryName(),
                    permissionCategory.isStatus()))
                    .collect(Collectors.toList());

            return permissionCategoryDTOs;

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Optional<PermissionCategoryDTO> getPermissionCategoryById(Long id) {
        try {
            // Lấy permission từ cơ sở dữ liệu
            Permission_Category permissionCategory = permissionCategoryRepository.findById(id).get();
            if (permissionCategory == null) {
                throw new Exception("Not found permission category with id: " + id);
            }

            // Chuyển đổi(permission) Entity sang DTO
            PermissionCategoryDTO permissionCategoryDTO = new PermissionCategoryDTO(permissionCategory.getPermissionCategoryId(),
                    permissionCategory.getPermissionCategoryName(),
                    permissionCategory.isStatus());
            return Optional.of(permissionCategoryDTO);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public OperationStatus createPermissionCategory(PermissionCategoryRequestDTO permissionCategoryDTO) {
        try {
            String name = permissionCategoryDTO.getName();
            if (name == null || name.isEmpty()) {
                return OperationStatus.INVALID_INPUT;
            }

            // Kiểm tra permission có tốn tại trong cơ sở dữ liệu hay không
            Optional<PermissionCategoryDTO> permisionCategory = getPermissionCategoryByName(permissionCategoryDTO.getName());
            if (permisionCategory.isPresent() && permisionCategory.get().isStatus()) {
                return OperationStatus.ALREADY_EXISTS;
            }

            // Chuyển đổi(permission) DTO sang Entity
            Permission_Category permissionCategory = new Permission_Category();
            permissionCategory.setPermissionCategoryName(permissionCategoryDTO.getName());
            permissionCategory.setStatus(true);

            // Lưu permission vào cơ sở dữ liệu
            Permission_Category savedPermissionCategory = permissionCategoryRepository.save(permissionCategory);
            if (savedPermissionCategory == null || savedPermissionCategory.getPermissionCategoryId() == null) {
                return OperationStatus.FAILURE;
            }

            return OperationStatus.SUCCESS;

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public OperationStatus updatePermissionCategory(PermissionCategoryRequestDTO permissionCategoryDTO) {
        try {
            Long id = permissionCategoryDTO.getId();
            String name = permissionCategoryDTO.getName();
            if (id == null || name == null || name.isEmpty()) {
                return OperationStatus.INVALID_INPUT;
            }

            // Lấy permission từ cơ sở dữ liệu
            Permission_Category permissionCategoryRoot = permissionCategoryRepository.findById(id).get();
            if (permissionCategoryRoot == null) {
                return OperationStatus.NOT_FOUND;
            }

            // Kiểm tra permission có tốn tại trong cơ sở dữ liệu hay không
            List<PermissionCategoryDTO> permisionCategories = getAllPermissionCategory();
                for (PermissionCategoryDTO permissionCategory : permisionCategories) {
                    if (permissionCategory.getName().equals(name) 
                        && !permissionCategory.getId().equals(id)
                        && permissionCategory.isStatus()) {
                        return OperationStatus.ALREADY_EXISTS;
                    }
                }

            // Chuyển đổi(permission) DTO sang Entity
            Permission_Category permissionCategory = new Permission_Category();
            permissionCategory.setPermissionCategoryId(id);
            permissionCategory.setPermissionCategoryName(permissionCategoryDTO.getName());
            permissionCategory.setStatus(permissionCategoryRoot.isStatus());

            // Lưu permission vào cơ sở dữ liệu
            Permission_Category savedPermissionCategory = permissionCategoryRepository.save(permissionCategory);
            if (savedPermissionCategory == null || savedPermissionCategory.getPermissionCategoryId() == null) {
                return OperationStatus.FAILURE;
            }

            return OperationStatus.SUCCESS;

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public OperationStatus deletePermissionCategory(Long id) {
        try {
            Permission_Category permissionCategory = permissionCategoryRepository.findById(id).get();
            if (permissionCategory == null) {
                return OperationStatus.NOT_FOUND;
            }

            // Xóa permission trong cơ sở dữ liệu
            permissionCategory.setStatus(false);
            Permission_Category savedPermissionCategory = permissionCategoryRepository.save(permissionCategory);
            if (savedPermissionCategory == null || savedPermissionCategory.getPermissionCategoryId() == null) {
                return OperationStatus.FAILURE;
            }

            return OperationStatus.SUCCESS;

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Optional<PermissionCategoryDTO> getPermissionCategoryByName(String name) {
        try {
            Optional<Permission_Category> permissionCategoryOptional = permissionCategoryRepository.findByPermissionCategoryName(name);
    
            // Kiểm tra xem giá trị có tồn tại hay không
            if (permissionCategoryOptional.isEmpty()) {
                return Optional.empty(); 
            }
    
            Permission_Category permissionCategory = permissionCategoryOptional.get();
    
            // Chuyển đổi Entity sang DTO
            PermissionCategoryDTO permissionCategoryDTO = new PermissionCategoryDTO(
                    permissionCategory.getPermissionCategoryId(),
                    permissionCategory.getPermissionCategoryName(),
                    permissionCategory.isStatus()
            );
    
            return Optional.of(permissionCategoryDTO);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching the Permission Category: " + e.getMessage());
        }
    }    
}
