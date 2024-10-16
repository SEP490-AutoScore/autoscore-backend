package com.CodeEvalCrew.AutoScore.models.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String databaseScript;

    private String databaseDescription;

    private String databaseName;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] databaseImage;

    private String databaseNote;

    private boolean status;

    private LocalDateTime createdAt;

    private Long createdBy;

    private LocalDateTime updatedAt;

    private Long updatedBy;

    private LocalDateTime deletedAt;

    private Long deletedBy;

}
