package com.CodeEvalCrew.AutoScore.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SignInWithGoogleResponseDTO;
import com.CodeEvalCrew.AutoScore.services.authentication.ISingInWithGoogleService;

@RestController
@RequestMapping("/api/auth")
public class SignInWithGoogleController {

    @Autowired
    private ISingInWithGoogleService singInWithGoogleService;
    
    // Sign in with Google
    @GetMapping("/signingoogle")
    public ResponseEntity<?> signInWithGoogle(OAuth2AuthenticationToken token) {
        try {
            SignInWithGoogleResponseDTO signInWithGoogleResponseDTO = singInWithGoogleService.authenticateWithGoogle(token.getPrincipal().getAttributes().get("email").toString());
            return ResponseEntity.ok(signInWithGoogleResponseDTO);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}
