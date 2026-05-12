package id.ac.ui.cs.advprog.auth.service.authprovider;

public record PasswordAuthRequest(String email, String password) implements AuthRequest {
}
