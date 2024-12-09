package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.AIName_Enum;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor

public class CreateAIApiKeyDTO {
    @NotNull
    private AIName_Enum aiName;

    @NotNull
    private String aiApiKey;

    private boolean shared = false;
}
