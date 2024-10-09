// package com.CodeEvalCrew.AutoScore.controllers;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import com.CodeEvalCrew.AutoScore.repositories.account_repository.IOAuthRefreshTokenRepository;
// import com.CodeEvalCrew.AutoScore.security.JwtTokenProvider;
// import com.CodeEvalCrew.AutoScore.services.authentication.ISingInWithGoogleService;
// import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SignInWithGoogleResponseDTO;
// import com.CodeEvalCrew.AutoScore.models.Entity.Account;

// import java.sql.Timestamp;
// import java.util.stream.Collectors;

// import com.CodeEvalCrew.AutoScore.models.Entity.Account_Role;

// @RestController
// @RequestMapping("/api/auth")
// public class AuthController {

//     private final IOAuthRefreshTokenRepository refreshTokenRepository;
//     private final JwtTokenProvider jwtTokenProvider;
//     private final ISingInWithGoogleService singInWithGoogleService;

//     @Autowired
//     public AuthController(IOAuthRefreshTokenRepository refreshTokenRepository, JwtTokenProvider jwtTokenProvider, ISingInWithGoogleService singInWithGoogleService) {
//         this.singInWithGoogleService = singInWithGoogleService;
//         this.refreshTokenRepository = refreshTokenRepository;
//         this.jwtTokenProvider = jwtTokenProvider;
//     }

//     @GetMapping("/signingoogle")
//     public ResponseEntity<?> signInWithGoogle(@RequestParam("email") String email) {
//         try {
//             if (email == null) {
//                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email not found");
//             }
//             SignInWithGoogleResponseDTO responseDTO = singInWithGoogleService.authenticateWithGoogle(email);
//             if (responseDTO == null) {
//                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
//             }
//             return ResponseEntity.ok(responseDTO);
//         } catch (IllegalStateException e) {
//             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
//         }
//     }

//     @PostMapping("/refresh-token")
//     public ResponseEntity<?> refreshToken(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody String refreshToken) {
//         // Lấy access token từ header nếu có
//         final String oldAccessToken;
//         if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//             oldAccessToken = authorizationHeader.substring(7); // Loại bỏ tiền tố "Bearer "
//         } else {
//             oldAccessToken = null;
//         }

//         return refreshTokenRepository.findByToken(refreshToken)
//                 .filter(oauthRefreshToken -> oauthRefreshToken.getExpiryDate().after(new Timestamp(System.currentTimeMillis())))
//                 .map(oauthRefreshToken -> {
//                     Account account = oauthRefreshToken.getAccount();
//                     if (account == null || account.getAccountId() == null) {
//                         throw new IllegalStateException("Account or account_id cannot be null");
//                     }

//                     // Thu hồi access token cũ (nếu có)
//                     if (oldAccessToken != null) {
//                         jwtTokenProvider.revokeToken(oldAccessToken, account);
//                     }

//                     // Tạo access token mới
//                     String newAccessToken = jwtTokenProvider.generateToken(
//                             account.getEmail(),
//                             account.getAccountRoles().stream()
//                                     .filter(Account_Role::isStatus)
//                                     .map(role -> role.getRole().getRoleName())
//                                     .collect(Collectors.toSet()),
//                             account.getAccountRoles().stream()
//                                     .filter(Account_Role::isStatus)
//                                     .flatMap(role -> role.getRole().getRole_permissions().stream())
//                                     .map(rolePermission -> rolePermission.getPermission().getAction())
//                                     .collect(Collectors.toSet())
//                     );

//                     return ResponseEntity.ok(newAccessToken);
//                 })
//                 .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token"));
//     }

// }
