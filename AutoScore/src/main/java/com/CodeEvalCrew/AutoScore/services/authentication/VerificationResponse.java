package com.CodeEvalCrew.AutoScore.services.authentication;
import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResponse {
    private HttpStatus status;
    private String message;
}

