// package com.CodeEvalCrew.AutoScore.services.authentication;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;

// import com.CodeEvalCrew.AutoScore.mappers.AccountMapper;
// import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SignInWithGoogleResponseDTO;
// import com.CodeEvalCrew.AutoScore.models.Entity.Account;
// import com.CodeEvalCrew.AutoScore.models.Entity.OAuthRefreshToken;
// import com.CodeEvalCrew.AutoScore.models.Entity.Role_Permission;
// import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;
// import com.CodeEvalCrew.AutoScore.repositories.account_repository.IOAuthRefreshTokenRepository;
// import com.CodeEvalCrew.AutoScore.security.JwtTokenProvider;

// import java.security.SecureRandom;
// import java.math.BigInteger;
// import java.sql.Timestamp;
// import java.time.Instant;
// import java.util.stream.Collectors;

// import com.CodeEvalCrew.AutoScore.models.Entity.Account_Role;

// @Service
// public class SignInWithGoogleService implements ISingInWithGoogleService {

//     private final IAccountRepository accountRepository;
//     private final IOAuthRefreshTokenRepository refreshTokenRepository;
//     private final JwtTokenProvider jwtTokenProvider;
//     private final SecureRandom secureRandom = new SecureRandom();

//     @Value("${jwt.expiration}")
//     private long jwtExpiration;

//     @Autowired
//     public SignInWithGoogleService(IAccountRepository accountRepository,
//             IOAuthRefreshTokenRepository refreshTokenRepository,
//             JwtTokenProvider jwtTokenProvider) {
//         this.accountRepository = accountRepository;
//         this.refreshTokenRepository = refreshTokenRepository;
//         this.jwtTokenProvider = jwtTokenProvider;
//     }

//     @Override
//     public SignInWithGoogleResponseDTO authenticateWithGoogle(String email) {
//         Account account = accountRepository.findByEmail(email)
//                 .orElseThrow(() -> new IllegalStateException("Account not found for email: " + email));

//         SignInWithGoogleResponseDTO response = AccountMapper.INSTANCE.accountToSignInWithGoogleResponseDTO(account);

//         // Tạo JWT token
//         String jwtToken = jwtTokenProvider.generateToken(
//                 account.getEmail(),
//                 account.getAccountRoles().stream()
//                         // Lọc ra những Account_Role có status là true
//                         .filter(Account_Role::isStatus)
//                         // Lấy tên role
//                         .map(role -> role.getRole().getRoleName())
//                         .collect(Collectors.toSet()),
//                 account.getAccountRoles().stream()
//                         // Lọc ra những Account_Role có status là true
//                         .filter(Account_Role::isStatus)
//                         // Lấy tất cả các quyền hạn từ role_permissions, chỉ lấy những quyền hợp lệ
//                         .flatMap(role -> role.getRole().getRole_permissions().stream()
//                         .filter(Role_Permission::isStatus)) // Lọc Role_Permission có status là true
//                         .map(rolePermission -> rolePermission.getPermission().getAction()) // Lấy action của Permission
//                         .collect(Collectors.toSet())
//         );
//         response.setJwtToken(jwtToken);

//         // Tạo refresh token ngẫu nhiên
//         String refreshToken = generateRefreshToken();

//         // Lưu refresh token vào cơ sở dữ liệu
//         OAuthRefreshToken oauthRefreshToken = new OAuthRefreshToken();
//         oauthRefreshToken.setToken(refreshToken);
//         oauthRefreshToken.setAccount(account);
//         oauthRefreshToken.setExpiryDate(Timestamp.from(Instant.now().plusMillis(jwtExpiration)));

//         refreshTokenRepository.save(oauthRefreshToken);
//         response.setRefreshToken(refreshToken);

//         return response;
//     }

//     private String generateRefreshToken() {
//         return new BigInteger(130, secureRandom).toString(32);
//     }
// }
