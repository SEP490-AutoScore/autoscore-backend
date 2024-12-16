package com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO;

import lombok.Data;

@Data
public class SingInWithEmailRequestDTO {
    private String email;
    private String password;
}
