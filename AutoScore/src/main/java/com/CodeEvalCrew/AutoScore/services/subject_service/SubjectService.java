package com.CodeEvalCrew.AutoScore.services.subject_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.SubjectRequest.CreateSubjectRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.SubjectRequest.DeleteSubjectRequest;
import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.SubjectRequest.UpdateSubjectRequest;
import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.models.Entity.Department;
import com.CodeEvalCrew.AutoScore.models.Entity.Subject;
import com.CodeEvalCrew.AutoScore.repositories.subject_repository.ISubjectRepository;
import com.CodeEvalCrew.AutoScore.repositories.department_repository.IDepartmentRepository;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class SubjectService implements ISubjectService {

    @Autowired
    private ISubjectRepository subjectRepository;

    @Autowired
    private IDepartmentRepository departmentRepository;

    @Autowired
    private IAccountRepository accountRepository;

  

    @Override
    public Page<Subject> getSubjectByCode(String subjectCode, Pageable pageable) {
        // Here we assume that we can retrieve a paginated list by subject code.
        // This would typically mean filtering, which may return more than one subject.
        return subjectRepository.findBySubjectCode(subjectCode, pageable);
    }

    @Override
    public Page<Subject> getAllSubjects(Pageable pageable) {
        return subjectRepository.findAll(pageable);
    }

    @Override
    public Subject createSubject(CreateSubjectRequest request) {
        // Kiểm tra xem Department có tồn tại không
        Optional<Department> departmentOpt = departmentRepository.findById(request.getDepartmentId());
        if (!departmentOpt.isPresent()) {
            throw new RuntimeException("Department not found with ID: " + request.getDepartmentId());
        }
        Department department = departmentOpt.get();
    
        // Kiểm tra xem Account (createBy) có tồn tại không
        Optional<Account> accountOpt = accountRepository.findById(request.getCreateBy());
        if (!accountOpt.isPresent()) {
            throw new RuntimeException("Account not found with ID: " + request.getCreateBy());
        }
        Account creator = accountOpt.get();
    
        // Tạo đối tượng Subject mới
        Subject subject = new Subject();
        subject.setSubjectName(request.getSubjectName());
        subject.setSubjectCode(request.getSubjectCode());
        subject.setDepartment(department);
        
        // Thiết lập status là "1"
        subject.setStatus("1"); // Gán giá trị status là "1"
        
        subject.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        subject.setCreatedBy(creator);
    
        // Bạn có thể thiết lập các trường khác nếu cần
    
        return subjectRepository.save(subject);
    }
    @Override
    public Subject updateSubject(UpdateSubjectRequest request) {
        Optional<Subject> optionalSubject = subjectRepository.findById(request.getSubjectId());
        
        if (optionalSubject.isPresent()) {
            Subject subject = optionalSubject.get();
    
            // Kiểm tra status trước khi cập nhật
            if (!subject.getStatus().equals("1")) {
                throw new RuntimeException("Cannot update subject with ID: " + request.getSubjectId() + " because it is not active.");
            }
    
            // Cập nhật thông tin subject
            subject.setSubjectName(request.getSubjectName());
            subject.setSubjectCode(request.getSubjectCode());
    
            // Kiểm tra Department ID
            if (request.getDepartmentId() != null) {
                Optional<Department> departmentOpt = departmentRepository.findById(request.getDepartmentId());
                if (departmentOpt.isPresent()) {
                    subject.setDepartment(departmentOpt.get());
                } else {
                    throw new RuntimeException("Department not found with ID: " + request.getDepartmentId());
                }
            }
    
            subject.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            // Cập nhật thông tin người cập nhật
            Optional<Account> accountOpt = accountRepository.findById(request.getUpdateBy());
            if (accountOpt.isPresent()) {
                subject.setUpdatedBy(accountOpt.get());
            } else {
                throw new RuntimeException("Account not found with ID: " + request.getUpdateBy());
            }
    
            return subjectRepository.save(subject);
        } else {
            throw new RuntimeException("Subject not found");
        }
    }
   @Override
public void deleteSubject(DeleteSubjectRequest request) {
    Optional<Subject> subjectOpt = subjectRepository.findById(request.getSubjectId());
    if (subjectOpt.isPresent()) {
        Subject subject = subjectOpt.get();
        
        // Cập nhật trạng thái subject thành 0
        subject.setStatus("0"); // Hoặc giá trị khác tùy theo cấu trúc của bạn
        subject.setDeletedAt(new Timestamp(System.currentTimeMillis())); // Cập nhật thời gian sửa đổi
        // Cập nhật thông tin người xóa
        Optional<Account> accountOpt = accountRepository.findById(request.getDeletedBy());
        if (accountOpt.isPresent()) {
            subject.setDeletedBy(accountOpt.get());
        } else {
            throw new RuntimeException("Account not found with ID: " + request.getDeletedBy());
        }
        
        subjectRepository.save(subject); // Lưu lại thay đổi
    } else {
        throw new RuntimeException("Subject not found with ID: " + request.getSubjectId());
    }
}

}

    

    
    
    

