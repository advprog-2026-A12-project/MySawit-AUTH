package id.ac.ui.cs.advprog.auth.service;

import id.ac.ui.cs.advprog.auth.dto.request.LoginRequest;
import id.ac.ui.cs.advprog.auth.dto.request.LogoutRequest;
import id.ac.ui.cs.advprog.auth.dto.request.RefreshTokenRequest;
import id.ac.ui.cs.advprog.auth.dto.request.RegisterRequest;
import id.ac.ui.cs.advprog.auth.dto.response.LoginResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.RegisterResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.TokenRefreshResponseData;

public interface AuthService {

    RegisterResponseData register(RegisterRequest request);

    LoginResponseData login(LoginRequest request);

    void logout(LogoutRequest request);

    TokenRefreshResponseData refresh(RefreshTokenRequest request);
}
