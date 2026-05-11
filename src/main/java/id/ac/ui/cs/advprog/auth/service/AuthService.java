package id.ac.ui.cs.advprog.auth.service;

import id.ac.ui.cs.advprog.auth.dto.request.auth.LoginRequest;
import id.ac.ui.cs.advprog.auth.dto.request.auth.RegisterRequest;
import id.ac.ui.cs.advprog.auth.dto.request.auth.GoogleLoginRequest;
import id.ac.ui.cs.advprog.auth.dto.response.auth.LoginResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.auth.RegisterResponseData;

public interface AuthService {

    RegisterResponseData register(RegisterRequest request);

    LoginResponseData login(LoginRequest request);

    LoginResponseData loginWithGoogle(GoogleLoginRequest request);

    void logout();
}
