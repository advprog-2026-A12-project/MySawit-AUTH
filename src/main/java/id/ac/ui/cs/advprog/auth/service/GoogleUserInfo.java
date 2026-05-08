package id.ac.ui.cs.advprog.auth.service;

public record GoogleUserInfo(
        String providerUserId,
        String email,
        String name
) {
}
