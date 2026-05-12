package id.ac.ui.cs.advprog.auth.service.utils;

public record IssuedTokens(
        String accessToken,
        long expiresIn
) {
}
