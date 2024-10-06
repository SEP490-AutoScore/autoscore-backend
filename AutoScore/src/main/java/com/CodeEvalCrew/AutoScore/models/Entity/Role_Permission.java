package com.CodeEvalCrew.AutoScore.models.Entity;

import java.time.LocalDateTime;

import io.micrometer.common.lang.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Role_Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long rolePermissionId;

    private boolean status;

    @Nullable
    @Past // Thời điểm tạo phải là trong quá khứ
    private LocalDateTime createdAt;

    @Nullable
    private Long createdBy;

    @Nullable
    private LocalDateTime updatedAt;

    @Nullable
    private Long updatedBy;

    @Nullable
    private LocalDateTime deletedAt;

    @Nullable
    private Long deletedBy;

    // Many-to-One relationship with Account
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    @ToString.Exclude
    private Role role;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "permission_id", nullable = false)
    @ToString.Exclude
    private Permission permission;
}
