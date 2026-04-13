package id.ac.ui.cs.advprog.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import id.ac.ui.cs.advprog.auth.exception.UnauthorizedException;
import id.ac.ui.cs.advprog.auth.exception.UnprocessableEntityException;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class GoogleOAuthServiceTest {

    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    private static final String TOKEN_INFO_ENDPOINT = "https://oauth2.googleapis.com/tokeninfo";
    private static final String CLIENT_ID = "client-id.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "client-secret";

    private MockRestServiceServer server;
    private GoogleOAuthService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        service = new GoogleOAuthService(builder.build(), CLIENT_ID, CLIENT_SECRET, "postmessage");
    }

    @Test
    void authenticateSuccessUsesDefaultRedirectUriAndFallbackName() {
        String idToken = "id-token-1";
        stubSuccessfulTokenExchange("auth-code", "redirect_uri=postmessage", idToken);

        server.expect(requestTo(TOKEN_INFO_ENDPOINT + "?id_token=" + idToken))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(validTokenInfoJson(
                        CLIENT_ID,
                        "https://accounts.google.com",
                        "true",
                        String.valueOf(Instant.now().plusSeconds(3600).getEpochSecond()),
                        "sub-123",
                        "john@example.com",
                        null
                ), MediaType.APPLICATION_JSON));

        GoogleUserInfo userInfo = service.authenticate("auth-code", " ");

        assertEquals("sub-123", userInfo.providerUserId());
        assertEquals("john@example.com", userInfo.email());
        assertEquals("john", userInfo.name());
        server.verify();
    }

    @Test
    void authenticateSuccessUsesProvidedRedirectUriAndName() {
        String idToken = "id-token-2";
        stubSuccessfulTokenExchange(
                "auth-code-2",
                "redirect_uri=http%3A%2F%2Flocalhost%3A3000%2Fapi%2Fauth%2Fcallback%2Fgoogle",
                idToken
        );

        server.expect(requestTo(TOKEN_INFO_ENDPOINT + "?id_token=" + idToken))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(validTokenInfoJson(
                        CLIENT_ID,
                        "accounts.google.com",
                        "true",
                        String.valueOf(Instant.now().plusSeconds(3600).getEpochSecond()),
                        "sub-456",
                        "jane@example.com",
                        "Jane Doe"
                ), MediaType.APPLICATION_JSON));

        GoogleUserInfo userInfo = service.authenticate(
                "auth-code-2",
                "http://localhost:3000/api/auth/callback/google"
        );

        assertEquals("Jane Doe", userInfo.name());
        server.verify();
    }

    @Test
    void authenticateThrowsWhenGoogleOAuthNotConfigured() {
        GoogleOAuthService misconfigured = new GoogleOAuthService(
                RestClient.create(),
                "",
                CLIENT_SECRET,
                "postmessage"
        );

        assertThrows(UnprocessableEntityException.class,
                () -> misconfigured.authenticate("auth-code", "postmessage"));
    }

    @Test
    void authenticateThrowsWhenTokenExchangeReturnsNoIdToken() {
        server.expect(requestTo(TOKEN_ENDPOINT))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThrows(UnauthorizedException.class,
                () -> service.authenticate("auth-code", "postmessage"));
    }

    @Test
    void authenticateThrowsWhenTokenExchangeReturnsBadRequest() {
        server.expect(requestTo(TOKEN_ENDPOINT))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest());

        assertThrows(UnauthorizedException.class,
                () -> service.authenticate("auth-code", "postmessage"));
    }

    @Test
    void authenticateThrowsWhenTokenInfoAudienceInvalid() {
        stubInvalidTokenInfo(validTokenInfoJson(
                "another-client-id",
                "https://accounts.google.com",
                "true",
                String.valueOf(Instant.now().plusSeconds(3600).getEpochSecond()),
                "sub-123",
                "john@example.com",
                "John"
        ));
    }

    @Test
    void authenticateThrowsWhenTokenInfoIssuerInvalid() {
        stubInvalidTokenInfo(validTokenInfoJson(
                CLIENT_ID,
                "https://malicious.example.com",
                "true",
                String.valueOf(Instant.now().plusSeconds(3600).getEpochSecond()),
                "sub-123",
                "john@example.com",
                "John"
        ));
    }

    @Test
    void authenticateThrowsWhenTokenInfoEmailNotVerified() {
        stubInvalidTokenInfo(validTokenInfoJson(
                CLIENT_ID,
                "https://accounts.google.com",
                "false",
                String.valueOf(Instant.now().plusSeconds(3600).getEpochSecond()),
                "sub-123",
                "john@example.com",
                "John"
        ));
    }

    @Test
    void authenticateThrowsWhenTokenInfoExpired() {
        stubInvalidTokenInfo(validTokenInfoJson(
                CLIENT_ID,
                "https://accounts.google.com",
                "true",
                String.valueOf(Instant.now().minusSeconds(10).getEpochSecond()),
                "sub-123",
                "john@example.com",
                "John"
        ));
    }

    @Test
    void authenticateThrowsWhenTokenInfoHasNonNumericExpiration() {
        stubInvalidTokenInfo(validTokenInfoJson(
                CLIENT_ID,
                "https://accounts.google.com",
                "true",
                "not-a-number",
                "sub-123",
                "john@example.com",
                "John"
        ));
    }

    @Test
    void authenticateThrowsWhenTokenInfoSubMissing() {
        stubInvalidTokenInfo(validTokenInfoJson(
                CLIENT_ID,
                "https://accounts.google.com",
                "true",
                String.valueOf(Instant.now().plusSeconds(3600).getEpochSecond()),
                "",
                "john@example.com",
                "John"
        ));
    }

    @Test
    void authenticateThrowsWhenTokenInfoEmailMissing() {
        stubInvalidTokenInfo(validTokenInfoJson(
                CLIENT_ID,
                "https://accounts.google.com",
                "true",
                String.valueOf(Instant.now().plusSeconds(3600).getEpochSecond()),
                "sub-123",
                "",
                "John"
        ));
    }

    @Test
    void authenticateThrowsWhenTokenInfoEndpointReturnsBadRequest() {
        String idToken = "id-token-bad-info";
        stubSuccessfulTokenExchange("auth-code", "redirect_uri=postmessage", idToken);

        server.expect(requestTo(TOKEN_INFO_ENDPOINT + "?id_token=" + idToken))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withBadRequest());

        assertThrows(UnauthorizedException.class,
                () -> service.authenticate("auth-code", "postmessage"));
    }

    @Test
    void authenticateThrowsWhenTokenInfoResponseMalformed() {
        String idToken = "id-token-malformed";
        stubSuccessfulTokenExchange("auth-code", "redirect_uri=postmessage", idToken);

        server.expect(requestTo(TOKEN_INFO_ENDPOINT + "?id_token=" + idToken))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("not-json", MediaType.APPLICATION_JSON));

        assertThrows(UnauthorizedException.class,
                () -> service.authenticate("auth-code", "postmessage"));
    }

    private void stubInvalidTokenInfo(String tokenInfoJson) {
        String idToken = "id-token-invalid";
        stubSuccessfulTokenExchange("auth-code", "redirect_uri=postmessage", idToken);

        server.expect(requestTo(TOKEN_INFO_ENDPOINT + "?id_token=" + idToken))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(tokenInfoJson, MediaType.APPLICATION_JSON));

        assertThrows(UnauthorizedException.class,
                () -> service.authenticate("auth-code", "postmessage"));
    }

    private void stubSuccessfulTokenExchange(String authCode, String expectedRedirectPart, String idToken) {
        server.expect(requestTo(TOKEN_ENDPOINT))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("code=" + authCode)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("client_id=" + CLIENT_ID)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("client_secret=" + CLIENT_SECRET)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(expectedRedirectPart)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("grant_type=authorization_code")))
                .andRespond(withSuccess("{\"id_token\":\"" + idToken + "\"}", MediaType.APPLICATION_JSON));
    }

    private String validTokenInfoJson(
            String audience,
            String issuer,
            String emailVerified,
            String expiration,
            String sub,
            String email,
            String name) {
        String namePart = (name == null) ? "" : ",\"name\":\"" + name + "\"";
        return "{"
                + "\"sub\":\"" + sub + "\"," 
                + "\"email\":\"" + email + "\"," 
                + "\"email_verified\":\"" + emailVerified + "\"," 
                + "\"aud\":\"" + audience + "\"," 
                + "\"iss\":\"" + issuer + "\"," 
                + "\"exp\":\"" + expiration + "\""
                + namePart
                + "}";
    }
}
