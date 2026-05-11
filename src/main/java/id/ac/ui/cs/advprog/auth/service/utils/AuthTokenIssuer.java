package id.ac.ui.cs.advprog.auth.service.utils;

import id.ac.ui.cs.advprog.auth.model.User;
import id.ac.ui.cs.advprog.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthTokenIssuer {

    private final JwtService jwtService;

    public IssuedTokens issue(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        return new IssuedTokens(accessToken, jwtService.getAccessTokenExpiration());
    }
}
