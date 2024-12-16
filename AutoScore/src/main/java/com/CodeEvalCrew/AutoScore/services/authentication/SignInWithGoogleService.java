package com.CodeEvalCrew.AutoScore.services.authentication;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Value("${jwt.access-token.expiration}")
    public long getJwtAccessExpiration;

    @Value("${jwt.refresh-token.expiration}")
    public long getJwtRefreshExpiration;

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
        return authenticate(account);
    }

    private String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public SignInWithGoogleResponseDTO authenticateWithEmail(String email, String password) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Account not found for email: " + email));

        if (!account.getPassword().equals(password)) {
            throw new IllegalStateException("Invalid password for email: " + email);
        }

        return authenticate(account);
    }

    private SignInWithGoogleResponseDTO authenticate(Account account){
        SignInWithGoogleResponseDTO response = AccountMapper.INSTANCE.accountToSignInWithGoogleResponseDTO(account);

        // Lấy Role duy nhất của Account
        Role role = account.getRole();
        if (role == null || !role.isStatus()) {
            throw new IllegalStateException("Account does not have a valid active role");
        }

        // Lấy tên Role và các quyền từ Role_Permissions
        String roleName = role.getRoleCode();
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
        response.setAvatar(account.getAvatar());

        Timestamp accessExpire = new Timestamp(Instant.now().plusMillis(getJwtAccessExpiration).toEpochMilli());
        Timestamp refreshExpire = new Timestamp(Instant.now().plusMillis(getJwtRefreshExpiration).toEpochMilli());

        // Tạo refresh token ngẫu nhiên
        String refreshToken = generateRefreshToken();
        // Lưu refresh token vào cơ sở dữ liệu
        OAuthRefreshToken oauthRefreshToken = new OAuthRefreshToken();
        oauthRefreshToken.setToken(refreshToken);
        oauthRefreshToken.setAccount(account);
        oauthRefreshToken.setExpiryDate(refreshExpire);

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
        response.setExp(accessExpire.getTime());

        return response;
    }
}
