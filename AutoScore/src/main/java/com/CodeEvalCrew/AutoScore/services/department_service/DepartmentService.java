package com.CodeEvalCrew.AutoScore.services.department_service;

import com.CodeEvalCrew.AutoScore.mappers.DepartmentMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.DepartmentRequest.DepartmentCreateRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.DepartmentRequest.DepartmentUpdateRequestDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.DepartmentResponseDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.DepartmentResponseMessage;
import com.CodeEvalCrew.AutoScore.models.Entity.Department;
import com.CodeEvalCrew.AutoScore.repositories.department_repository.IDepartmentRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class DepartmentService implements IDepartmentService {

    private final IDepartmentRepository departmentRepository;

    @Autowired
    public DepartmentService(IDepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public Page<DepartmentResponseDTO> getAllDepartments(Pageable pageable) {
        Page<Department> departmentsPage = departmentRepository.findAll(pageable);
        return departmentsPage.map(DepartmentMapper.INSTANCE::departmentToResponse);
    }

    @Override
    public Page<DepartmentResponseDTO> getDepartmentsByDevLanguage(String devLanguage, Pageable pageable) {
        Page<Department> departmentsPage = departmentRepository.findByDevLanguageIgnoreCase(devLanguage, pageable);
        return departmentsPage.map(DepartmentMapper.INSTANCE::departmentToResponse);
    }

    @Override
    public ResponseEntity<Object> createDepartment(DepartmentCreateRequestDTO request) {
        // Kiểm tra nếu departmentName và devLanguage trùng nhau
        if (request.getDepartmentName().equals(request.getDevLanguage())) {
            return ResponseEntity.badRequest().body("Department name and development language must not be the same.");
        }

        // Kiểm tra xem devLanguage đã tồn tại trong database chưa
        List<Department> existingDepartments = departmentRepository.findByDevLanguage(request.getDevLanguage());
        if (!existingDepartments.isEmpty()) {
            return ResponseEntity.badRequest().body("Development language already exists in the database.");
        }

        // Tạo mới department
        Department newDepartment = new Department();
        newDepartment.setDepartmentName(request.getDepartmentName());
        newDepartment.setDevLanguage(request.getDevLanguage());
        newDepartment.setStatus(true);

        // Lưu department vào database
        Department savedDepartment = departmentRepository.save(newDepartment);

        // Tạo một Response DTO để trả về thông tin department mới
        DepartmentResponseDTO responseDTO = new DepartmentResponseDTO();
        responseDTO.setDepartmentId(savedDepartment.getDepartmentId()); // Sửa lại để sử dụng departmentId
        responseDTO.setDepartmentName(savedDepartment.getDepartmentName());
        responseDTO.setDevLanguage(savedDepartment.getDevLanguage());
        responseDTO.setStatus(savedDepartment.isStatus()); // Sử dụng phương thức isStatus()

        // Trả về thông báo thành công cùng với thông tin department mới
        return ResponseEntity.ok().body(new DepartmentResponseMessage("Create successfully!", responseDTO));
    }

    @Override
    public ResponseEntity<Object> updateDepartment(DepartmentUpdateRequestDTO request) {
        // Kiểm tra nếu departmentName và devLanguage trùng nhau
        if (request.getDepartmentName().equals(request.getDevLanguage())) {
            return ResponseEntity.badRequest().body("Department name and development language must not be the same.");
        }

        // Tìm phòng ban hiện tại
        Department existingDepartment = departmentRepository.findById(request.getId()).orElse(null);
        if (existingDepartment == null) {
            return ResponseEntity.notFound().build();
        }

        // Kiểm tra xem department có status là true không
        if (!existingDepartment.isStatus()) {
            return ResponseEntity.badRequest().body("Cannot update department with status false.");
        }

        // Kiểm tra xem devLanguage đã tồn tại trong database chưa, ngoại trừ phòng ban
        // hiện tại
        List<Department> existingDepartments = departmentRepository.findByDevLanguage(request.getDevLanguage());
        if (!existingDepartments.isEmpty() && existingDepartments.stream()
                .noneMatch(department -> department.getDepartmentId().equals(request.getId()))) {
            return ResponseEntity.badRequest().body("Development language already exists in the database.");
        }

        // Cập nhật thông tin phòng ban
        existingDepartment.setDepartmentName(request.getDepartmentName());
        existingDepartment.setDevLanguage(request.getDevLanguage());

        // Lưu cập nhật vào database
        Department updatedDepartment = departmentRepository.save(existingDepartment);

        // Tạo một Response DTO để trả về thông tin department đã cập nhật
        DepartmentResponseDTO responseDTO = new DepartmentResponseDTO();
        responseDTO.setDepartmentId(updatedDepartment.getDepartmentId());
        responseDTO.setDepartmentName(updatedDepartment.getDepartmentName());
        responseDTO.setDevLanguage(updatedDepartment.getDevLanguage());
        responseDTO.setStatus(updatedDepartment.isStatus());

        // Trả về thông báo thành công cùng với thông tin department đã cập nhật
        return ResponseEntity.ok().body(new DepartmentResponseMessage("Update successfully!", responseDTO));
    }

    @Override
    public ResponseEntity<Object> deleteDepartment(Long id) {
        // Tìm phòng ban theo id
        Department existingDepartment = departmentRepository.findById(id).orElse(null);
        if (existingDepartment == null) {
            return ResponseEntity.notFound().build();
        }

        // Kiểm tra trạng thái hiện tại
        if (existingDepartment.isStatus()) {
            // Chuyển trạng thái thành false
            existingDepartment.setStatus(false);
            departmentRepository.save(existingDepartment);
            // Tạo một Response DTO để trả về thông tin department vừa mới xóa
            DepartmentResponseDTO responseDTO = DepartmentMapper.INSTANCE.departmentToResponse(existingDepartment);
            return ResponseEntity.ok().body("You can click again to enable! " + responseDTO.toString());
        } else {
            // Chuyển trạng thái thành true
            existingDepartment.setStatus(true);
            departmentRepository.save(existingDepartment);
            // Tạo một Response DTO để trả về thông tin department vừa mới kích hoạt lại
            DepartmentResponseDTO responseDTO = DepartmentMapper.INSTANCE.departmentToResponse(existingDepartment);
            return ResponseEntity.ok().body("You can click again to disable! " + responseDTO.toString());
        }
    }

}
