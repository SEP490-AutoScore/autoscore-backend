package com.CodeEvalCrew.AutoScore.controllers;

import java.util.Map;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class SignInWithGoogleController {
    
    // Sign in with Google
    @GetMapping("/signingoogle")
    public Map <String, Object> signInWithGoogle(OAuth2AuthenticationToken token) {
        return token.getPrincipal().getAttributes();
    }
    
}
