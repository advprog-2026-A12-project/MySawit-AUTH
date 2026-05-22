package id.ac.ui.cs.advprog.auth.service.wallet;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    private static final String CREATE_WALLET_PATH = "/api/v1/internal/wallets";

    private MockRestServiceServer server;
    private RestClient restClient;
    private RestClientPaymentWalletClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        restClient = builder.build();
        client = new RestClientPaymentWalletClient(restClient, BASE_URL + "/", API_KEY, CREATE_WALLET_PATH);
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
    void constructorAcceptsConfiguredProperties() {
        RestClientPaymentWalletClient configuredClient = new RestClientPaymentWalletClient(
                BASE_URL,
                API_KEY,
                CREATE_WALLET_PATH
        );

        assertNotNull(configuredClient);
    }

    @Test
    void createWalletNormalizesBaseUrlAndPathSlashes() {
        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        RestClientPaymentWalletClient clientWithSlashlessPath = new RestClientPaymentWalletClient(
                restClient,
                BASE_URL + "///",
                API_KEY,
                "api/v1/internal/wallets"
        );

        server.expect(requestTo(BASE_URL + "/api/v1/internal/wallets"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(successResponse(walletId), MediaType.APPLICATION_JSON));

        clientWithSlashlessPath.createWallet(userId);

        server.verify();
    }

    @Test
    void createWalletWorksWhenBaseUrlHasNoTrailingSlash() {
        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        RestClientPaymentWalletClient clientWithoutTrailingSlash = new RestClientPaymentWalletClient(
                restClient,
                BASE_URL,
                API_KEY,
                CREATE_WALLET_PATH
        );

        server.expect(requestTo(BASE_URL + "/api/v1/internal/wallets"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(successResponse(walletId), MediaType.APPLICATION_JSON));

        clientWithoutTrailingSlash.createWallet(userId);

        server.verify();
    }

    @Test
    void createWalletThrowsWhenBaseUrlMissing() {
        RestClientPaymentWalletClient misconfigured = new RestClientPaymentWalletClient(
                RestClient.create(),
                "",
                API_KEY,
                CREATE_WALLET_PATH
        );
        UUID userId = UUID.randomUUID();

        assertThrows(ExternalServiceException.class, () -> misconfigured.createWallet(userId));
    }

    @Test
    void createWalletThrowsWhenBaseUrlNull() {
        RestClientPaymentWalletClient misconfigured = new RestClientPaymentWalletClient(
                RestClient.create(),
                null,
                API_KEY,
                CREATE_WALLET_PATH
        );
        UUID userId = UUID.randomUUID();

        assertThrows(ExternalServiceException.class, () -> misconfigured.createWallet(userId));
    }

    @Test
    void createWalletThrowsWhenApiKeyMissing() {
        RestClientPaymentWalletClient misconfigured = new RestClientPaymentWalletClient(
                RestClient.create(),
                BASE_URL,
                " ",
                CREATE_WALLET_PATH
        );
        UUID userId = UUID.randomUUID();

        assertThrows(ExternalServiceException.class, () -> misconfigured.createWallet(userId));
    }

    @Test
    void createWalletThrowsWhenApiKeyNull() {
        RestClientPaymentWalletClient misconfigured = new RestClientPaymentWalletClient(
                RestClient.create(),
                BASE_URL,
                null,
                CREATE_WALLET_PATH
        );
        UUID userId = UUID.randomUUID();

        assertThrows(ExternalServiceException.class, () -> misconfigured.createWallet(userId));
    }

    @Test
    void createWalletThrowsWhenPathMissing() {
        RestClientPaymentWalletClient misconfigured = new RestClientPaymentWalletClient(
                RestClient.create(),
                BASE_URL,
                API_KEY,
                " "
        );
        UUID userId = UUID.randomUUID();

        assertThrows(ExternalServiceException.class, () -> misconfigured.createWallet(userId));
    }

    @Test
    void createWalletThrowsWhenPathNull() {
        RestClientPaymentWalletClient misconfigured = new RestClientPaymentWalletClient(
                RestClient.create(),
                BASE_URL,
                API_KEY,
                null
        );
        UUID userId = UUID.randomUUID();

        assertThrows(ExternalServiceException.class, () -> misconfigured.createWallet(userId));
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
    void createWalletThrowsWhenPaymentResponseMalformed() {
        UUID userId = UUID.randomUUID();

        server.expect(requestTo(BASE_URL + "/api/v1/internal/wallets"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("not-json", MediaType.APPLICATION_JSON));

        assertThrows(ExternalServiceException.class, () -> client.createWallet(userId));
        server.verify();
    }

    @Test
    void createWalletThrowsWhenResponseStatusInvalid() {
        UUID userId = UUID.randomUUID();

        server.expect(requestTo(BASE_URL + "/api/v1/internal/wallets"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"status\":\"error\",\"data\":{\"walletId\":\""
                        + UUID.randomUUID()
                        + "\"}}", MediaType.APPLICATION_JSON));

        assertThrows(ExternalServiceException.class, () -> client.createWallet(userId));
        server.verify();
    }

    @Test
    void createWalletThrowsWhenResponseDataMissing() {
        UUID userId = UUID.randomUUID();

        server.expect(requestTo(BASE_URL + "/api/v1/internal/wallets"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"status\":\"success\",\"data\":null}", MediaType.APPLICATION_JSON));

        assertThrows(ExternalServiceException.class, () -> client.createWallet(userId));
        server.verify();
    }

    @Test
    void createWalletThrowsWhenResponseWalletIdMissing() {
        UUID userId = UUID.randomUUID();

        server.expect(requestTo(BASE_URL + "/api/v1/internal/wallets"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"status\":\"success\",\"data\":{}}", MediaType.APPLICATION_JSON));

        assertThrows(ExternalServiceException.class, () -> client.createWallet(userId));
        server.verify();
    }

    @Test
    void createWalletThrowsWhenResponseBodyMissing() {
        UUID userId = UUID.randomUUID();

        server.expect(requestTo(BASE_URL + "/api/v1/internal/wallets"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

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
