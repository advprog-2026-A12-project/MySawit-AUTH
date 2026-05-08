package id.ac.ui.cs.advprog.auth.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import id.ac.ui.cs.advprog.auth.exception.UnauthorizedException;
import id.ac.ui.cs.advprog.auth.exception.UnprocessableEntityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
public class GoogleOAuthService {

    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    private static final String TOKEN_INFO_ENDPOINT = "https://oauth2.googleapis.com/tokeninfo";

    private final RestClient restClient;
    private final String googleClientId;
    private final String googleClientSecret;
    private final String defaultRedirectUri;

    @Autowired
    public GoogleOAuthService(
            @Value("${google.client-id:}") String googleClientId,
            @Value("${google.client-secret:}") String googleClientSecret,
            @Value("${google.redirect-uri:postmessage}") String defaultRedirectUri) {
        this.restClient = RestClient.create();
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        this.defaultRedirectUri = defaultRedirectUri;
    }

    GoogleOAuthService(
            RestClient restClient,
            String googleClientId,
            String googleClientSecret,
            String defaultRedirectUri) {
        this.restClient = restClient;
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        this.defaultRedirectUri = defaultRedirectUri;
    }

    public GoogleUserInfo authenticate(String authorizationCode, String redirectUri) {
        ensureGoogleOAuthConfigured();
        String effectiveRedirectUri = (redirectUri == null || redirectUri.isBlank())
                ? defaultRedirectUri
                : redirectUri;

        GoogleTokenExchangeResponse tokenResponse = exchangeCodeForIdToken(
                authorizationCode,
                effectiveRedirectUri
        );

        return verifyIdToken(tokenResponse.idToken());
    }

    private void ensureGoogleOAuthConfigured() {
        if (googleClientId == null || googleClientId.isBlank()
                || googleClientSecret == null || googleClientSecret.isBlank()) {
            throw new UnprocessableEntityException("Google OAuth is not configured");
        }
    }

    private GoogleTokenExchangeResponse exchangeCodeForIdToken(
            String authorizationCode,
            String redirectUri) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", authorizationCode);
        form.add("client_id", googleClientId);
        form.add("client_secret", googleClientSecret);
        form.add("redirect_uri", redirectUri);
        form.add("grant_type", "authorization_code");

        try {
            GoogleTokenExchangeResponse response = restClient.post()
                    .uri(TOKEN_ENDPOINT)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(GoogleTokenExchangeResponse.class);

            if (response == null || response.idToken() == null || response.idToken().isBlank()) {
                throw new UnauthorizedException("Google authentication failed");
            }
            return response;
        } catch (RestClientResponseException ex) {
            throw new UnauthorizedException("Google authentication failed");
        }
    }

    private GoogleUserInfo verifyIdToken(String idToken) {
        try {
            GoogleTokenInfoResponse tokenInfo = restClient.get()
                    .uri(TOKEN_INFO_ENDPOINT + "?id_token={idToken}", idToken)
                    .retrieve()
                    .body(GoogleTokenInfoResponse.class);

            if (tokenInfo == null || !isValidTokenInfo(tokenInfo)) {
                throw new UnauthorizedException("Invalid Google token");
            }

            String resolvedName = tokenInfo.name();
            if (resolvedName == null || resolvedName.isBlank()) {
                resolvedName = tokenInfo.email().split("@")[0];
            }

            return new GoogleUserInfo(tokenInfo.sub(), tokenInfo.email(), resolvedName);
        } catch (RestClientResponseException ex) {
            throw new UnauthorizedException("Invalid Google token");
        } catch (RestClientException ex) {
            throw new UnauthorizedException("Google authentication failed");
        }
    }

    private boolean isValidTokenInfo(GoogleTokenInfoResponse tokenInfo) {
        return isValidAudience(tokenInfo.aud())
                && isValidIssuer(tokenInfo.iss())
                && isEmailVerified(tokenInfo.emailVerified())
                && !isExpired(tokenInfo.exp())
                && tokenInfo.sub() != null
                && !tokenInfo.sub().isBlank()
                && tokenInfo.email() != null
                && !tokenInfo.email().isBlank();
    }

    private boolean isValidAudience(String audience) {
        return googleClientId.equals(audience);
    }

    private boolean isValidIssuer(String issuer) {
        return "accounts.google.com".equals(issuer)
                || "https://accounts.google.com".equals(issuer);
    }

    private boolean isEmailVerified(String emailVerified) {
        return "true".equalsIgnoreCase(emailVerified);
    }

    private boolean isExpired(String exp) {
        try {
            long expEpochSeconds = Long.parseLong(exp);
            long nowEpochSeconds = System.currentTimeMillis() / 1000;
            return expEpochSeconds <= nowEpochSeconds;
        } catch (NumberFormatException ex) {
            return true;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoogleTokenExchangeResponse(
            @JsonProperty("id_token") String idToken
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoogleTokenInfoResponse(
            String sub,
            String email,
            @JsonProperty("email_verified") String emailVerified,
            String aud,
            String iss,
            String exp,
            String name
    ) {
    }
}
