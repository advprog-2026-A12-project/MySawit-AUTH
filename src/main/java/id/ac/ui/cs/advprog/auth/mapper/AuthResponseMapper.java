package id.ac.ui.cs.advprog.auth.mapper;

import id.ac.ui.cs.advprog.auth.dto.response.auth.LoginResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.auth.RegisterResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.auth.TokenRefreshResponseData;
import id.ac.ui.cs.advprog.auth.model.User;
import id.ac.ui.cs.advprog.auth.service.utils.IssuedTokens;

import org.springframework.stereotype.Component;

@Component
public class AuthResponseMapper {

    public RegisterResponseData toRegisterResponse(User user) {
        return RegisterResponseData.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public LoginResponseData toLoginResponse(IssuedTokens tokens) {
        return LoginResponseData.builder()
                .accessToken(tokens.accessToken())
                .refreshToken(tokens.refreshToken())
                .tokenType("Bearer")
                .expiresIn((int) tokens.expiresIn())
                .build();
    }

    public TokenRefreshResponseData toRefreshResponse(IssuedTokens tokens) {
        return TokenRefreshResponseData.builder()
                .accessToken(tokens.accessToken())
                .refreshToken(tokens.refreshToken())
                .tokenType("Bearer")
                .expiresIn((int) tokens.expiresIn())
                .build();
    }
}
