package id.ac.ui.cs.advprog.auth.controller;

import id.ac.ui.cs.advprog.auth.dto.request.LoginRequest;
import id.ac.ui.cs.advprog.auth.dto.request.LogoutRequest;
import id.ac.ui.cs.advprog.auth.dto.request.RefreshTokenRequest;
import id.ac.ui.cs.advprog.auth.dto.request.RegisterRequest;
import id.ac.ui.cs.advprog.auth.dto.response.BaseResponse;
import id.ac.ui.cs.advprog.auth.dto.response.LoginResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.RegisterResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.TokenRefreshResponseData;
import id.ac.ui.cs.advprog.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<RegisterResponseData>> register(
            @Valid @RequestBody RegisterRequest request) {
        RegisterResponseData data = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("Registration successful", data));
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponseData>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponseData data = authService.login(request);
        return ResponseEntity.ok(BaseResponse.success("Login successful", data));
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(BaseResponse.success("Logout successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<TokenRefreshResponseData>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        TokenRefreshResponseData data = authService.refresh(request);
        return ResponseEntity.ok(BaseResponse.success("Token refreshed successfully", data));
    }
}
