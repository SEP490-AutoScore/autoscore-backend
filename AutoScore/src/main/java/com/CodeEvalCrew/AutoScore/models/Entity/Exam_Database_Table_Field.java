package com.CodeEvalCrew.AutoScore.models.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Exam_Database_Table_Field {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long examDatabaseTableFieldId;

    private String fieldName;

    private String fieldType;

    private boolean isNotNull;
    private boolean isUnique;
    private boolean status;
    private boolean isPrimaryKey;
    private boolean isForeignKey;
    private String referencedTable;
    private String referencedField;
    private LocalDateTime createdAt;

    private Long createdBy;

    private LocalDateTime updatedAt;

    private Long updatedBy;

    private LocalDateTime deletedAt;

    private Long deletedBy;

    @ManyToOne
    @JoinColumn(name = "examDatabaseTableId", nullable = false)
    private Exam_Database_Table examDatabaseTable;
}
