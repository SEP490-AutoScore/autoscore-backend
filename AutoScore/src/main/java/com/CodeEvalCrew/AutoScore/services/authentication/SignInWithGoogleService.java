package com.CodeEvalCrew.AutoScore.services.authentication;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.mappers.AccountMapper;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.SignInWithGoogleResponseDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.models.Entity.Employee;
import com.CodeEvalCrew.AutoScore.models.Entity.OAuthRefreshToken;
import com.CodeEvalCrew.AutoScore.models.Entity.Role;
import com.CodeEvalCrew.AutoScore.models.Entity.Role_Permission;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IEmployeeRepository;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IOAuthRefreshTokenRepository;
import com.CodeEvalCrew.AutoScore.security.JwtTokenProvider;

@Service
public class SignInWithGoogleService implements ISingInWithGoogleService {

    private final IAccountRepository accountRepository;
    private final IEmployeeRepository employeeRepository;
    private final IOAuthRefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${jwt.access-token.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-token.expiration}")
    public long getJwtRefreshExpiration;

    @Autowired
    public SignInWithGoogleService(IAccountRepository accountRepository,
                                   IOAuthRefreshTokenRepository refreshTokenRepository,
                                   JwtTokenProvider jwtTokenProvider,
                                   IEmployeeRepository employeeRepository) {
        this.accountRepository = accountRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public SignInWithGoogleResponseDTO authenticateWithGoogle(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Account not found for email: " + email));

        SignInWithGoogleResponseDTO response = AccountMapper.INSTANCE.accountToSignInWithGoogleResponseDTO(account);

        // Lấy Role duy nhất của Account
        Role role = account.getRole();
        if (role == null || !role.isStatus()) {
            throw new IllegalStateException("Account does not have a valid active role");
        }

        // Lấy tên Role và các quyền từ Role_Permissions
        String roleName = role.getRoleName();
        Set<String> permissions = role.getRole_permissions().stream()
                .filter(Role_Permission::isStatus)
                .map(rolePermission -> rolePermission.getPermission().getAction())
                .collect(Collectors.toSet());

        // Tạo JWT token với role và permissions
        String jwtToken = jwtTokenProvider.generateToken(
                account.getEmail(),
                roleName,
                permissions
        );
        response.setJwtToken(jwtToken);

        // Tạo refresh token ngẫu nhiên
        String refreshToken = generateRefreshToken();

        // Lưu refresh token vào cơ sở dữ liệu
        OAuthRefreshToken oauthRefreshToken = new OAuthRefreshToken();
        oauthRefreshToken.setToken(refreshToken);
        oauthRefreshToken.setAccount(account);
        oauthRefreshToken.setExpiryDate(Timestamp.from(Instant.now().plusMillis(getJwtRefreshExpiration)));

        refreshTokenRepository.save(oauthRefreshToken);
        response.setRefreshToken(refreshToken);

        Employee employee = employeeRepository.findByAccount_AccountId(account.getAccountId());
        // Lấy tên campus
        String campus = employee.getOrganization().getName();
        // Lấy tên employee
        String employeeName = employee.getFullName();
        // Lấy position
        String position = employee.getPosition().getName();
        
        response.setName(employeeName);
        response.setCampus(campus);
        response.setPosition(position);
        

        return response;
    }

    private String generateRefreshToken() {
        return new BigInteger(130, secureRandom).toString(32);
    }
}

