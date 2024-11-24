package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.AIName_Enum;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAIApiKeyDTO {
    @NotNull
    private AIName_Enum aiName;

    @NotNull
    private String aiApiKey;

    private boolean isShared;
}
