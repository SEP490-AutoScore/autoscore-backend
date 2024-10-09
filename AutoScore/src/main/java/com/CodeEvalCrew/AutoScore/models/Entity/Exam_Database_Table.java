package com.CodeEvalCrew.AutoScore.models.Entity;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
public class Exam_Database_Table {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long examDatabaseTableId;

    private String tableName;

    private String tableScript;

    private boolean status;

    private LocalDateTime createdAt;

    private Long createdBy;

    private LocalDateTime updatedAt;

    private Long updatedBy;

    private LocalDateTime deletedAt;

    private Long deletedBy;
    //rlsp
    //1-n table field
    @OneToMany
    @JoinColumn(name = "examDatabaseTableFieldId", nullable = true)
    private Set<Exam_Database_Table_Field> examDatabaseTableFields;
    //n-1 db
    @ManyToOne
    @JoinColumn(name = "examDatabaseId", nullable = false)
    private Exam_Database examDatabase;
}