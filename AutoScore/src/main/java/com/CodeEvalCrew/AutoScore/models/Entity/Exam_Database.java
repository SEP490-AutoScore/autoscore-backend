package com.CodeEvalCrew.AutoScore.models.Entity;

import java.time.LocalDateTime;

import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Exam_Status_Enum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
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

    private Exam_Status_Enum status;

    private LocalDateTime createdAt;

    private Long createdBy;

    private LocalDateTime updatedAt;

    private Long updatedBy;

    private LocalDateTime deletedAt;

    private Long deletedBy;

    //1-1 exam 
    @OneToOne
    @JoinColumn(name = "examPaperId", nullable = false)
    private Exam_Paper examPaper;

}
