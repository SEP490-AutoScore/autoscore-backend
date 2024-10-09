package com.CodeEvalCrew.AutoScore.models.Entity;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
public class Exam_Database {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long examDatabaseId;

    private String databaseScript;

    private String databaseDescription;

    private String databaseName;

    private String databaseImage;

    private String databaseNote;

    private boolean status;

    private LocalDateTime createdAt;

    private Long createdBy;

    private LocalDateTime updatedAt;

    private Long updatedBy;

    private LocalDateTime deletedAt;

    private Long deletedBy;

    //Relationship
    @OneToMany
    @JoinColumn(name = "examDatabaseTableId", nullable = true)
    private Set<Exam_Database_Table> examDatabaseTables;
}
