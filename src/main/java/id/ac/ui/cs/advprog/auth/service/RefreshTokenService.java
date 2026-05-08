package id.ac.ui.cs.advprog.auth.service;

import id.ac.ui.cs.advprog.auth.exception.InvalidTokenException;
import id.ac.ui.cs.advprog.auth.model.RefreshToken;
import id.ac.ui.cs.advprog.auth.model.User;
import id.ac.ui.cs.advprog.auth.repository.RefreshTokenRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public void persist(User user, String rawRefreshToken) {
        String hash = jwtService.hashToken(rawRefreshToken);
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hash)
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTokenExpiration()))
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    public void revokeIfPresent(String rawRefreshToken) {
        String tokenHash = jwtService.hashToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(rt -> {
            if (!rt.isRevoked()) {
                rt.setRevoked(true);
                refreshTokenRepository.save(rt);
            }
        });
    }

    public User validateAndRevoke(String rawRefreshToken) {
        String tokenHash = jwtService.hashToken(rawRefreshToken);

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (storedToken.isRevoked()) {
            refreshTokenRepository.revokeAllByUser(storedToken.getUser());
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        if (storedToken.isExpired()) {
            throw new InvalidTokenException("Refresh token has expired");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        return storedToken.getUser();
    }
}
