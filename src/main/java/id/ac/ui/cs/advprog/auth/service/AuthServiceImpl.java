package id.ac.ui.cs.advprog.auth.service;

import id.ac.ui.cs.advprog.auth.dto.request.LoginRequest;
import id.ac.ui.cs.advprog.auth.dto.request.LogoutRequest;
import id.ac.ui.cs.advprog.auth.dto.request.RefreshTokenRequest;
import id.ac.ui.cs.advprog.auth.dto.request.RegisterRequest;
import id.ac.ui.cs.advprog.auth.dto.response.LoginResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.LoginUserDto;
import id.ac.ui.cs.advprog.auth.dto.response.RegisterResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.TokenRefreshResponseData;
import id.ac.ui.cs.advprog.auth.enums.UserRole;
import id.ac.ui.cs.advprog.auth.exception.DuplicateUserException;
import id.ac.ui.cs.advprog.auth.exception.InvalidTokenException;
import id.ac.ui.cs.advprog.auth.exception.InvalidUserRequestException;
import id.ac.ui.cs.advprog.auth.exception.UnauthorizedException;
import id.ac.ui.cs.advprog.auth.exception.UnprocessableEntityException;
import id.ac.ui.cs.advprog.auth.model.RefreshToken;
import id.ac.ui.cs.advprog.auth.model.User;
import id.ac.ui.cs.advprog.auth.repository.RefreshTokenRepository;
import id.ac.ui.cs.advprog.auth.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public RegisterResponseData register(RegisterRequest request) {
        UserRole role = parseAndValidateRole(request.getRole());

        validateMandorCertification(role, request.getMandorCertificationNumber());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("Email");
        }

        if (request.getMandorCertificationNumber() != null && !request.getMandorCertificationNumber().isBlank() && userRepository.existsByMandorCertificationNumber( request.getMandorCertificationNumber())) {
            throw new DuplicateUserException("Mandor certification number");
        }

        String username = generateUniqueUsername(request.getName());

        User user = User.builder()
                .username(username)
                .email(request.getEmail())
                .name(request.getName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .mandorCertificationNumber(request.getMandorCertificationNumber())
                .isActive(true)
                .build();

        User saved = userRepository.save(user);

        return RegisterResponseData.builder()
                .id(saved.getId())
                .username(saved.getUsername())
                .email(saved.getEmail())
                .name(saved.getName())
                .role(saved.getRole().name())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public LoginResponseData login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String rawRefreshToken = jwtService.generateRefreshToken();

        persistRefreshToken(user, rawRefreshToken);

        LoginUserDto userDto = LoginUserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();

        return LoginResponseData.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresIn((int) jwtService.getAccessTokenExpiration())
                .user(userDto)
                .build();
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        String tokenHash = jwtService.hashToken(request.getRefreshToken());
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(rt -> {
            if (!rt.isRevoked()) {
                rt.setRevoked(true);
                refreshTokenRepository.save(rt);
            }
        });
    }

    @Override
    @Transactional
    public TokenRefreshResponseData refresh(RefreshTokenRequest request) {
        String tokenHash = jwtService.hashToken(request.getRefreshToken());

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (storedToken.isRevoked()) {
            // Potential token reuse attack — revoke all tokens for this user
            refreshTokenRepository.revokeAllByUser(storedToken.getUser());
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        if (storedToken.isExpired()) {
            throw new InvalidTokenException("Refresh token has expired");
        }
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        User user = storedToken.getUser();
        if (!user.isActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRawRefreshToken = jwtService.generateRefreshToken();

        persistRefreshToken(user, newRawRefreshToken);

        return TokenRefreshResponseData.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRawRefreshToken)
                .tokenType("Bearer")
                .expiresIn((int) jwtService.getAccessTokenExpiration())
                .build();
    }

    private UserRole parseAndValidateRole(String roleStr) {
        UserRole role;
        try {
            role = UserRole.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidUserRequestException("Invalid role: " + roleStr);
        }
        if (role == UserRole.ADMIN) {
            throw new UnprocessableEntityException("ADMIN role is not allowed for registration");
        }
        return role;
    }

    private void validateMandorCertification(UserRole role, String certNumber) {
        if (role == UserRole.MANDOR && (certNumber == null || certNumber.isBlank())) {
            throw new UnprocessableEntityException(
                    "mandorCertificationNumber is required for MANDOR role");
        }
    }

    private void persistRefreshToken(User user, String rawRefreshToken) {
        String hash = jwtService.hashToken(rawRefreshToken);
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hash)
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTokenExpiration()))
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    String generateUniqueUsername(String name) {
        String slug = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        String candidate;
        do {
            byte[] bytes = new byte[2];
            secureRandom.nextBytes(bytes);
            String suffix = HexFormat.of().formatHex(bytes);
            candidate = slug + "-" + suffix;
            if (candidate.length() > 50) {
                candidate = candidate.substring(0, 50);
            }
        } while (userRepository.existsByUsername(candidate));

        return candidate;
    }
}
