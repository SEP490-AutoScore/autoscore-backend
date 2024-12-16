package com.CodeEvalCrew.AutoScore.services.authentication;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SignInWithGoogleResponseDTO;

public interface  ISingInWithGoogleService {
    SignInWithGoogleResponseDTO authenticateWithGoogle(String email);
    SignInWithGoogleResponseDTO authenticateWithEmail(String email, String password);
}
