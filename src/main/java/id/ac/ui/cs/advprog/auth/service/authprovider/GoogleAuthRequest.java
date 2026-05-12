package id.ac.ui.cs.advprog.auth.service.authprovider;

public record GoogleAuthRequest(String authorizationCode, String redirectUri) implements AuthRequest {
}
