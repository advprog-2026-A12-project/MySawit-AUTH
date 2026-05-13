package id.ac.ui.cs.advprog.auth.service;

import id.ac.ui.cs.advprog.auth.dto.request.auth.LoginRequest;
import id.ac.ui.cs.advprog.auth.dto.request.auth.RegisterRequest;
import id.ac.ui.cs.advprog.auth.dto.request.auth.GoogleLoginRequest;
import id.ac.ui.cs.advprog.auth.dto.response.auth.LoginResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.auth.RegisterResponseData;
import id.ac.ui.cs.advprog.auth.enums.UserRole;
import id.ac.ui.cs.advprog.auth.mapper.AuthResponseMapper;
import id.ac.ui.cs.advprog.auth.model.User;
import id.ac.ui.cs.advprog.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.auth.service.authprovider.AuthProvider;
import id.ac.ui.cs.advprog.auth.service.authprovider.AuthProviderFactory;
import id.ac.ui.cs.advprog.auth.service.authprovider.AuthProviderType;
import id.ac.ui.cs.advprog.auth.service.authprovider.AuthRequest;
import id.ac.ui.cs.advprog.auth.service.authprovider.GoogleAuthRequest;
import id.ac.ui.cs.advprog.auth.service.authprovider.PasswordAuthRequest;
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
    private final AuthTokenIssuer authTokenIssuer;
    private final AuthProviderFactory authProviderFactory;
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
        User user = authenticate(
                AuthProviderType.PASSWORD,
                new PasswordAuthRequest(request.getEmail(), request.getPassword())
        );
        IssuedTokens tokens = authTokenIssuer.issue(user);
        return authResponseMapper.toLoginResponse(tokens);
    }

    @Override
    @Transactional
    public LoginResponseData loginWithGoogle(GoogleLoginRequest request) {
        User user = authenticate(
                AuthProviderType.GOOGLE,
            new GoogleAuthRequest(
                request.getAuthorizationCode(),
                request.getRedirectUri(),
                request.getRole(),
                request.getMandorCertificationNumber()
            )
        );
        IssuedTokens tokens = authTokenIssuer.issue(user);
        return authResponseMapper.toLoginResponse(tokens);
    }

    private User authenticate(AuthProviderType type, AuthRequest request) {
        AuthProvider provider = authProviderFactory.create(type);
        return provider.authenticate(request);
    }

    @Override
    @Transactional
    public void logout() {
    
    }

    String generateUniqueUsername(String name) {
        return usernameGenerator.generateUniqueUsername(name);
    }
}
