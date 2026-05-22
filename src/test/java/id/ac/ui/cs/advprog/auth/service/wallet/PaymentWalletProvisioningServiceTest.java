package id.ac.ui.cs.advprog.auth.service.wallet;

import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentWalletProvisioningServiceTest {

    @Mock
    private PaymentWalletClient paymentWalletClient;

    @Test
    void provisionWalletDelegatesToPaymentWalletClient() {
        UUID userId = UUID.randomUUID();
        PaymentWalletProvisioningService service = new PaymentWalletProvisioningService(paymentWalletClient);

        service.provisionWallet(userId);

        verify(paymentWalletClient).createWallet(userId);
    }
}
