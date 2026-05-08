package id.ac.ui.cs.advprog.auth.service;

import id.ac.ui.cs.advprog.auth.dto.request.auth.LoginRequest;
import id.ac.ui.cs.advprog.auth.dto.request.auth.LogoutRequest;
import id.ac.ui.cs.advprog.auth.dto.request.auth.RefreshTokenRequest;
import id.ac.ui.cs.advprog.auth.dto.request.auth.RegisterRequest;
import id.ac.ui.cs.advprog.auth.dto.request.auth.GoogleLoginRequest;
import id.ac.ui.cs.advprog.auth.dto.response.auth.LoginResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.auth.RegisterResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.auth.TokenRefreshResponseData;
import id.ac.ui.cs.advprog.auth.enums.UserRole;
import id.ac.ui.cs.advprog.auth.exception.UnauthorizedException;
import id.ac.ui.cs.advprog.auth.mapper.AuthResponseMapper;
import id.ac.ui.cs.advprog.auth.model.User;
import id.ac.ui.cs.advprog.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.auth.service.utils.AuthTokenIssuer;
import id.ac.ui.cs.advprog.auth.service.utils.IssuedTokens;
import id.ac.ui.cs.advprog.auth.service.utils.UsernameGenerator;
import id.ac.ui.cs.advprog.auth.validation.RegistrationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationValidator registrationValidator;
    private final UsernameGenerator usernameGenerator;
    private final RefreshTokenService refreshTokenService;
    private final AuthTokenIssuer authTokenIssuer;
    private final GoogleOAuthService googleOAuthService;
    private final AuthResponseMapper authResponseMapper;

    @Override
    @Transactional
    public RegisterResponseData register(RegisterRequest request) {
        UserRole role = registrationValidator.validate(request);
        String username = usernameGenerator.generateUniqueUsername(request.getName());

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

        return authResponseMapper.toRegisterResponse(saved);
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

        IssuedTokens tokens = authTokenIssuer.issue(user);
        return authResponseMapper.toLoginResponse(tokens);
    }

    @Override
    @Transactional
    public LoginResponseData loginWithGoogle(GoogleLoginRequest request) {
        GoogleUserInfo googleUserInfo = googleOAuthService.authenticate(
            request.getAuthorizationCode(),
            request.getRedirectUri());

        User user = userRepository
            .findByOauthProviderAndOauthProviderId("GOOGLE", googleUserInfo.providerUserId())
            .orElseGet(() -> findOrCreateGoogleUser(googleUserInfo));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        IssuedTokens tokens = authTokenIssuer.issue(user);
        return authResponseMapper.toLoginResponse(tokens);
    }

    private User findOrCreateGoogleUser(GoogleUserInfo googleUserInfo) {
        return userRepository.findByEmail(googleUserInfo.email())
                .map(existingUser -> linkGoogleAccount(existingUser, googleUserInfo))
                .orElseGet(() -> createGoogleUser(googleUserInfo));
    }

    private User linkGoogleAccount(User existingUser, GoogleUserInfo googleUserInfo) {
        if (existingUser.getOauthProvider() == null || existingUser.getOauthProvider().isBlank()) {
            existingUser.setOauthProvider("GOOGLE");
            existingUser.setOauthProviderId(googleUserInfo.providerUserId());
            return userRepository.save(existingUser);
        }

        boolean sameProvider = "GOOGLE".equalsIgnoreCase(existingUser.getOauthProvider());
        boolean sameProviderUserId = googleUserInfo.providerUserId()
                .equals(existingUser.getOauthProviderId());
        if (!sameProvider || !sameProviderUserId) {
            throw new UnauthorizedException("Google account is not linked to this user");
        }

        return existingUser;
    }

    private User createGoogleUser(GoogleUserInfo googleUserInfo) {
        String name = (googleUserInfo.name() == null || googleUserInfo.name().isBlank())
                ? googleUserInfo.email()
                : googleUserInfo.name();
        String username = usernameGenerator.generateUniqueUsername(name);

        User user = User.builder()
                .username(username)
                .email(googleUserInfo.email())
                .name(name)
                .passwordHash(null)
                .role(UserRole.BURUH)
                .oauthProvider("GOOGLE")
                .oauthProviderId(googleUserInfo.providerUserId())
                .isActive(true)
                .build();

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        refreshTokenService.revokeIfPresent(request.getRefreshToken());
    }

    @Override
    @Transactional
    public TokenRefreshResponseData refresh(RefreshTokenRequest request) {
        User user = refreshTokenService.validateAndRevoke(request.getRefreshToken());
        if (!user.isActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }
        IssuedTokens tokens = authTokenIssuer.issue(user);
        return authResponseMapper.toRefreshResponse(tokens);
    }

    String generateUniqueUsername(String name) {
        return usernameGenerator.generateUniqueUsername(name);
    }
}
