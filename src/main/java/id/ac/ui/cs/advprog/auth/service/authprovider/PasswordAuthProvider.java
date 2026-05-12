package id.ac.ui.cs.advprog.auth.service.authprovider;

import id.ac.ui.cs.advprog.auth.exception.UnauthorizedException;
import id.ac.ui.cs.advprog.auth.exception.UnprocessableEntityException;
import id.ac.ui.cs.advprog.auth.model.User;
import id.ac.ui.cs.advprog.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordAuthProvider implements AuthProvider {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthProviderType getType() {
        return AuthProviderType.PASSWORD;
    }

    @Override
    public User authenticate(AuthRequest request) {
        if (!(request instanceof PasswordAuthRequest passwordRequest)) {
            throw new UnprocessableEntityException("Invalid auth request");
        }

        User user = userRepository.findByEmail(passwordRequest.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(passwordRequest.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        return user;
    }
}
