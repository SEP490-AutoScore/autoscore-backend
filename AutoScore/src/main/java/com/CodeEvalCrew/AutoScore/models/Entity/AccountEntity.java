package com.CodeEvalCrew.AutoScore.models.Entity;

import java.sql.Timestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class AccountEntity {

    @Id
    private long account_id;

    @NotNull
    @Size(min = 2, max = 100) // Đảm bảo tên dài ít nhất 2 ký tự và tối đa 100 ký tự
    private String name;

    @NotNull
    @Email(message = "Email should be valid") // Kiểm tra định dạng email
    private String email;

    @NotNull
    @Size(min = 1, max = 20) // Đảm bảo trạng thái có độ dài hợp lệ
    private String status;

    @NotNull
    @Past // Thời điểm tạo phải là trong quá khứ
    private Timestamp createdAt;

    private long createdBy;

    private Timestamp updatedAt;

    private long updatedBy;

    private Timestamp deletedAt;

    private long deletedBy;

    @NotNull
    private long campus_id; // Ràng buộc campus_id không được null
}
