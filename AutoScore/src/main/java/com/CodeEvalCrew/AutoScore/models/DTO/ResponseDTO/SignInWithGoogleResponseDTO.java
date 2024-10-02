package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignInWithGoogleResponseDTO {
    private long accountId;
    
    @NotNull
    private String name;
    
    @NotNull
    @Email
    private String email;
    
    private String campusName;
    private String roleName;
    private Set<String> permissions;
    private String jwtToken;
    private String refreshToken;
}
