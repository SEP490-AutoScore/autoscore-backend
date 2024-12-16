package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import java.util.Base64;
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
    private String id;
    private String role;
    private String position;
    private String campus;
    private Set<String> permissions;
    private long exp;
    private String avatar;

    public void setAvatar(byte[] avatarBytes) {
        if (avatarBytes != null) {
            this.avatar = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(avatarBytes);
        } else {
            this.avatar = null;
        }
    }
}
