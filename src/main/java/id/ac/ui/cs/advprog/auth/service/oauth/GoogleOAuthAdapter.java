package id.ac.ui.cs.advprog.auth.service.oauth;

import id.ac.ui.cs.advprog.auth.service.GoogleOAuthService;
import id.ac.ui.cs.advprog.auth.service.utils.GoogleUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoogleOAuthAdapter implements OAuthClient {

    private final GoogleOAuthService googleOAuthService;

    @Override
    public GoogleUserInfo authenticate(String authorizationCode, String redirectUri) {
        return googleOAuthService.authenticate(authorizationCode, redirectUri);
    }
}
