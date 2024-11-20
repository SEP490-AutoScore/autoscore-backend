package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;
import java.time.LocalDateTime;

import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Exam_Status_Enum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamDatabaseDTO {
    private Long examDatabaseId;
    private String databaseScript;
    private String databaseDescription;
    private String databaseName;
    private byte[] databaseImage;
    private String databaseNote;
    private Exam_Status_Enum status;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;
    private LocalDateTime deletedAt;
    private Long deletedBy;
    private Long examPaperId;
}
