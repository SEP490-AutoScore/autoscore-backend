package com.CodeEvalCrew.AutoScore.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SignInWithGoogleControllerTest {

    @Mock
    private OAuth2AuthenticationToken token;

    @Mock
    private OAuth2User principal;

    @InjectMocks
    private SignInWithGoogleController signInWithGoogleController;

    @Test
    void testSignInWithGoogle_Success() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "John Doe");
        attributes.put("email", "john.doe@example.com");
        when(token.getPrincipal()).thenReturn(principal);
        when(principal.getAttributes()).thenReturn(attributes);

        // Act
        Map<String, Object> result = signInWithGoogleController.signInWithGoogle(token);

        // Assert
        assertEquals(attributes, result);
    }

    @Test
    void testSignInWithGoogle_Failure_InvalidToken() {
        // Arrange
        when(token.getPrincipal()).thenReturn(null);

        // Act and Assert
        assertThrows(NullPointerException.class, () -> signInWithGoogleController.signInWithGoogle(token));
    }

    @Test
    void testSignInWithGoogle_Failure_NullToken() {
        // Act and Assert
        assertThrows(NullPointerException.class, () -> signInWithGoogleController.signInWithGoogle(null));
    }
}