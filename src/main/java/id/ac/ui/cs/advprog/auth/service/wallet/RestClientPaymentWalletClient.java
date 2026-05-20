package id.ac.ui.cs.advprog.auth.service.wallet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import id.ac.ui.cs.advprog.auth.exception.ExternalServiceException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
public class RestClientPaymentWalletClient implements PaymentWalletClient {

    private static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";
    private static final String SUCCESS_STATUS = "success";

    private final RestClient restClient;
    private final String paymentServiceBaseUrl;
    private final String internalApiKey;
    private final String createWalletPath;

    @Autowired
    public RestClientPaymentWalletClient(
            @Value("${payment.service.base-url:}") String paymentServiceBaseUrl,
            @Value("${payment.internal-api-key:}") String internalApiKey,
            @Value("${payment.wallet.create-path:/api/v1/internal/wallets}") String createWalletPath) {
        this(RestClient.create(), paymentServiceBaseUrl, internalApiKey, createWalletPath);
    }

    RestClientPaymentWalletClient(
            RestClient restClient,
            String paymentServiceBaseUrl,
            String internalApiKey,
            String createWalletPath) {
        this.restClient = restClient;
        this.paymentServiceBaseUrl = trimTrailingSlash(paymentServiceBaseUrl);
        this.internalApiKey = internalApiKey;
        this.createWalletPath = ensureLeadingSlash(createWalletPath);
    }

    @Override
    public void createWallet(UUID userId) {
        ensureConfigured();
        ensureUserIdPresent(userId);

        try {
            CreateWalletResponse response = restClient.post()
                    .uri(paymentServiceBaseUrl + createWalletPath)
                    .header(INTERNAL_API_KEY_HEADER, internalApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CreateWalletRequest(userId))
                    .retrieve()
                    .body(CreateWalletResponse.class);

            validateResponse(response);
        } catch (RestClientResponseException ex) {
            throw new ExternalServiceException("Wallet service rejected wallet creation", ex);
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Wallet service is unavailable", ex);
        }
    }

    private void ensureUserIdPresent(UUID userId) {
        if (userId == null) {
            throw new ExternalServiceException("User id is required to create wallet");
        }
    }

    private void ensureConfigured() {
        if (paymentServiceBaseUrl == null || paymentServiceBaseUrl.isBlank()) {
            throw new ExternalServiceException("Payment service URL is not configured");
        }
        if (internalApiKey == null || internalApiKey.isBlank()) {
            throw new ExternalServiceException("Payment internal API key is not configured");
        }
        if (createWalletPath == null || createWalletPath.isBlank()) {
            throw new ExternalServiceException("Payment wallet creation path is not configured");
        }
    }

    private void validateResponse(CreateWalletResponse response) {
        if (response == null
                || !SUCCESS_STATUS.equalsIgnoreCase(response.status())
                || response.data() == null
                || response.data().walletId() == null) {
            throw new ExternalServiceException("Wallet service returned an invalid response");
        }
    }

    private static String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        int endIndex = value.length();
        while (endIndex > 0 && value.charAt(endIndex - 1) == '/') {
            endIndex--;
        }
        return value.substring(0, endIndex);
    }

    private static String ensureLeadingSlash(String value) {
        if (value == null || value.isBlank() || value.startsWith("/")) {
            return value;
        }
        return "/" + value;
    }

    private record CreateWalletRequest(UUID userId) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record CreateWalletResponse(
            String status,
            String message,
            CreateWalletResponseData data
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record CreateWalletResponseData(
            UUID walletId,
            Boolean alreadyProcessed
    ) {
    }
}
