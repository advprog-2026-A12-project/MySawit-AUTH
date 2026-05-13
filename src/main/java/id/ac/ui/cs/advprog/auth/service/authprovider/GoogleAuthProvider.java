package id.ac.ui.cs.advprog.auth.service.authprovider;

import id.ac.ui.cs.advprog.auth.enums.UserRole;
import id.ac.ui.cs.advprog.auth.exception.InvalidUserRequestException;
import id.ac.ui.cs.advprog.auth.exception.UnauthorizedException;
import id.ac.ui.cs.advprog.auth.exception.UnprocessableEntityException;
import id.ac.ui.cs.advprog.auth.model.User;
import id.ac.ui.cs.advprog.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.auth.service.oauth.OAuthClient;
import id.ac.ui.cs.advprog.auth.service.utils.GoogleUserInfo;
import id.ac.ui.cs.advprog.auth.service.utils.UsernameGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoogleAuthProvider implements AuthProvider {

    private static final String GOOGLE = "GOOGLE";

    private final UserRepository userRepository;
    private final OAuthClient oauthClient;
    private final UsernameGenerator usernameGenerator;

    @Override
    public AuthProviderType getType() {
        return AuthProviderType.GOOGLE;
    }

    @Override
    public User authenticate(AuthRequest request) {
        if (!(request instanceof GoogleAuthRequest googleRequest)) {
            throw new UnprocessableEntityException("Invalid auth request");
        }

        GoogleUserInfo googleUserInfo = oauthClient.authenticate(
                googleRequest.authorizationCode(),
                googleRequest.redirectUri());

        User user = userRepository
                .findByOauthProviderAndOauthProviderId(GOOGLE, googleUserInfo.providerUserId())
            .orElseGet(() -> findOrCreateGoogleUser(googleUserInfo, googleRequest.role(), googleRequest.mandorCertificationNumber()));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        return user;
    }

        private User findOrCreateGoogleUser(
            GoogleUserInfo googleUserInfo,
            String requestedRole,
            String mandorCertificationNumber
        ) {
        return userRepository.findByEmail(googleUserInfo.email())
                .map(existingUser -> linkGoogleAccount(existingUser, googleUserInfo))
            .orElseGet(() -> createGoogleUser(googleUserInfo, requestedRole, mandorCertificationNumber));
    }

    private User linkGoogleAccount(User existingUser, GoogleUserInfo googleUserInfo) {
        if (existingUser.getOauthProvider() == null || existingUser.getOauthProvider().isBlank()) {
            existingUser.setOauthProvider(GOOGLE);
            existingUser.setOauthProviderId(googleUserInfo.providerUserId());
            return userRepository.save(existingUser);
        }

        boolean sameProvider = GOOGLE.equalsIgnoreCase(existingUser.getOauthProvider());
        boolean sameProviderUserId = googleUserInfo.providerUserId()
                .equals(existingUser.getOauthProviderId());
        if (!sameProvider || !sameProviderUserId) {
            throw new UnauthorizedException("Google account is not linked to this user");
        }

        return existingUser;
    }

    private User createGoogleUser(GoogleUserInfo googleUserInfo, String requestedRole, String mandorCertificationNumber) {
        String name = (googleUserInfo.name() == null || googleUserInfo.name().isBlank())
                ? googleUserInfo.email()
                : googleUserInfo.name();
        String username = usernameGenerator.generateUniqueUsername(name);

        UserRole role = resolveRequestedRole(requestedRole, mandorCertificationNumber);

        User user = User.builder()
                .username(username)
                .email(googleUserInfo.email())
                .name(name)
                .passwordHash(null)
                .role(role)
                .mandorCertificationNumber(role == UserRole.MANDOR ? mandorCertificationNumber : null)
                .oauthProvider(GOOGLE)
                .oauthProviderId(googleUserInfo.providerUserId())
                .isActive(true)
                .build();

        return userRepository.save(user);
    }

    private UserRole resolveRequestedRole(String requestedRole, String mandorCertificationNumber) {
        if (requestedRole == null || requestedRole.isBlank()) {
            return UserRole.BURUH;
        }

        UserRole role;
        try {
            role = UserRole.valueOf(requestedRole.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidUserRequestException("Invalid role: " + requestedRole);
        }

        if (role == UserRole.ADMIN) {
            throw new UnprocessableEntityException("ADMIN role is not allowed for registration");
        }

        if (role == UserRole.MANDOR && (mandorCertificationNumber == null || mandorCertificationNumber.isBlank())) {
            throw new UnprocessableEntityException("mandorCertificationNumber is required for MANDOR role");
        }

        return role;
    }
}
