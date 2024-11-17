package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignInWithGoogleResponseDTO {
    private String jwtToken;
    private String refreshToken;
    @NotNull
    @Email
    private String email;
    private String name;
    private String role;
    private String position;
    private String campus;
    private Set<String> permissions;
}
