package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamDatabaseResponseDTO {
    private Long examDatabaseId;
    private String databaseScript;
    private String databaseDescription;
    private String databaseName;
    private byte[] databaseImage;
    private String databaseNote;
    // private boolean status;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;
    private LocalDateTime deletedAt;
    private Long deletedBy;

}
