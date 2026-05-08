package id.ac.ui.cs.advprog.auth.service.utils;

import id.ac.ui.cs.advprog.auth.model.User;
import id.ac.ui.cs.advprog.auth.service.JwtService;
import id.ac.ui.cs.advprog.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthTokenIssuer {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public IssuedTokens issue(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String rawRefreshToken = jwtService.generateRefreshToken();
        refreshTokenService.persist(user, rawRefreshToken);

        return new IssuedTokens(accessToken, rawRefreshToken, jwtService.getAccessTokenExpiration());
    }
}
