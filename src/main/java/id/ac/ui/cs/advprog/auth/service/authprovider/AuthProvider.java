package id.ac.ui.cs.advprog.auth.service.authprovider;

import id.ac.ui.cs.advprog.auth.model.User;

public interface AuthProvider {
    AuthProviderType getType();

    User authenticate(AuthRequest request);
}
