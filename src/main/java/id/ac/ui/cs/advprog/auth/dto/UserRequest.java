package id.ac.ui.cs.advprog.auth.dto;

import id.ac.ui.cs.advprog.auth.enums.UserRole;

public record UserRequest(
    String username,
    String email,
    String name,
    String password,
    UserRole role
) {}
