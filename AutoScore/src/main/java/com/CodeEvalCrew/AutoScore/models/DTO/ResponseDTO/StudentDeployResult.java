package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentDeployResult {
    private Long studentId;
    private boolean successful; 
    private String message;

    // Getter for successful (isSuccessful)
    public boolean isSuccessful() {
        return successful;
    }
}
