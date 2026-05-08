package id.ac.ui.cs.advprog.auth.service.authprovider;

public interface AuthProviderFactory {
    AuthProvider create(AuthProviderType type);
}
