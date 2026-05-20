package id.ac.ui.cs.advprog.auth.service.wallet;

import java.util.UUID;

public interface PaymentWalletClient {

    void createWallet(UUID userId);
}
