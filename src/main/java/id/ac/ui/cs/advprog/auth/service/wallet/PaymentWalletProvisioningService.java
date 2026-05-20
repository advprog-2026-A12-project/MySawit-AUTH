package id.ac.ui.cs.advprog.auth.service.wallet;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentWalletProvisioningService implements WalletProvisioningService {

    private final PaymentWalletClient paymentWalletClient;

    @Override
    public void provisionWallet(UUID userId) {
        paymentWalletClient.createWallet(userId);
    }
}
