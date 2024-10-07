package com.CodeEvalCrew.AutoScore.models.Entity;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subjectId;

    private String subjectName;

    private String subjectCode;

    // private boolean status;
      @NotNull
    @Size(min = 1, max = 20)
    private String status;

    // @NotNull
    // @Past // Thời điểm tạo phải là trong quá khứ
    // private Timestamp createdAt;

    // @ManyToOne
    // @JoinColumn(name = "created_by", referencedColumnName = "accountId")
    // private Account createdBy; // Nối với account_id

    // private Timestamp updatedAt;

    // @ManyToOne
    // @JoinColumn(name = "updated_by", referencedColumnName = "accountId")
    // private Account updatedBy; // Nối với account_id

    // private Timestamp deletedAt;

    // @ManyToOne
    // @JoinColumn(name = "deleted_by", referencedColumnName = "accountId")
    // private Account deletedBy; // Nối với account_id

    @Past
    private LocalDateTime createdAt;

    private Long createdBy;

    private LocalDateTime updatedAt;

    private Long updatedBy;

    private LocalDateTime deletedAt;

    private Long deletedBy;

    // 1-n exam
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
    private Set<Exam> exams;

    // 1-1 department
    @ManyToOne
    @JoinColumn(name = "departmentId", nullable = false)
    private Department department;
}
