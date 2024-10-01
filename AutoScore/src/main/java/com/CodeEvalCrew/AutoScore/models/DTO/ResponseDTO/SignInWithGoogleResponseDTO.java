package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import java.util.Set;

import com.CodeEvalCrew.AutoScore.models.Entity.Campus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignInWithGoogleResponseDTO {
    private long accountId;
    private String name;
    private String email;
    private String campusName;
    private String roleName;
    private Set<String> permissions;
}
