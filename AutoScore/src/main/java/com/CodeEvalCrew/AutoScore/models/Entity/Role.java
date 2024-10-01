package com.CodeEvalCrew.AutoScore.models.Entity;

import java.sql.Timestamp;
import java.util.Set;

import io.micrometer.common.lang.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Past;
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
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long roleId;

    private String roleName;

    private boolean status;

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
    
    @OneToMany(mappedBy = "role")
    private Set<Account_Role> account_roles;

    //n-n permision
    @OneToMany(mappedBy = "role")
    private Set<Role_Permission> role_permissions;
}
