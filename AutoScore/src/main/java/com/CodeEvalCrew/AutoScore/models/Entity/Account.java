package com.CodeEvalCrew.AutoScore.models.Entity;

import java.sql.Timestamp;
import java.util.Set;

import io.micrometer.common.lang.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "account")
public class Account {
    @Id
    private long accountId;

    @NotNull
    @Size(min = 2, max = 100) // Đảm bảo tên dài ít nhất 2 ký tự và tối đa 100 ký tự
    private String name;

    @NotNull
    @Email(message = "Email should be valid") // Kiểm tra định dạng email
    private String email;

    @NotNull
    @Size(min = 1, max = 20) // Đảm bảo trạng thái có độ dài hợp lệ
    private String status;

    @Nullable
    @Past // Thời điểm tạo phải là trong quá khứ
    private Timestamp createdAt;

    @Nullable
    private Long createdBy;

    @Nullable
    private Timestamp updatedAt;

    @Nullable
    private Long updatedBy;

    @Nullable
    private Timestamp deletedAt;

    @Nullable
    private Long deletedBy;
    
    //Relationship
    @OneToMany(mappedBy = "account")
    private Set<Account_Role> account_roles;

    @ManyToOne
    @JoinColumn(name = "campusId", nullable = false)
    private Campus campus;
}
