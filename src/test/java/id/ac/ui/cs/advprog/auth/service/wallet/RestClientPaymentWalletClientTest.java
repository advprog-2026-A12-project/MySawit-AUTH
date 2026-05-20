package id.ac.ui.cs.advprog.auth.service.wallet;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import id.ac.ui.cs.advprog.auth.exception.ExternalServiceException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class RestClientPaymentWalletClientTest {

    private static final String BASE_URL = "http://payment-service:8003";
    private static final String API_KEY = "internal-key";

    private MockRestServiceServer server;
    private RestClientPaymentWalletClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new RestClientPaymentWalletClient(builder.build(), BASE_URL + "/", API_KEY);
    }

    @Test
    void createWalletPostsExpectedRequest() {
        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        server.expect(requestTo(BASE_URL + "/api/v1/internal/wallets"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Internal-Api-Key", API_KEY))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"userId\":\"" + userId + "\"}"))
                .andRespond(withSuccess(successResponse(walletId), MediaType.APPLICATION_JSON));

        client.createWallet(userId);

        server.verify();
    }

    @Test
    void createWalletThrowsWhenBaseUrlMissing() {
        RestClientPaymentWalletClient misconfigured = new RestClientPaymentWalletClient(
                RestClient.create(),
                "",
                API_KEY
        );

        assertThrows(ExternalServiceException.class, () -> misconfigured.createWallet(UUID.randomUUID()));
    }

    @Test
    void createWalletThrowsWhenApiKeyMissing() {
        RestClientPaymentWalletClient misconfigured = new RestClientPaymentWalletClient(
                RestClient.create(),
                BASE_URL,
                " "
        );

        assertThrows(ExternalServiceException.class, () -> misconfigured.createWallet(UUID.randomUUID()));
    }

    @Test
    void createWalletThrowsWhenUserIdMissing() {
        assertThrows(ExternalServiceException.class, () -> client.createWallet(null));
    }

    @Test
    void createWalletThrowsWhenPaymentRejectsRequest() {
        UUID userId = UUID.randomUUID();

        server.expect(requestTo(BASE_URL + "/api/v1/internal/wallets"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThrows(ExternalServiceException.class, () -> client.createWallet(userId));
        server.verify();
    }

    @Test
    void createWalletThrowsWhenResponseInvalid() {
        UUID userId = UUID.randomUUID();

        server.expect(requestTo(BASE_URL + "/api/v1/internal/wallets"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"status\":\"error\",\"data\":null}", MediaType.APPLICATION_JSON));

        assertThrows(ExternalServiceException.class, () -> client.createWallet(userId));
        server.verify();
    }

    private String successResponse(UUID walletId) {
        return "{"
                + "\"status\":\"success\","
                + "\"message\":\"Wallet created successfully\","
                + "\"data\":{"
                + "\"walletId\":\"" + walletId + "\","
                + "\"alreadyProcessed\":false"
                + "},"
                + "\"timestamp\":\"2026-05-08T10:00:00Z\""
                + "}";
    }
}
