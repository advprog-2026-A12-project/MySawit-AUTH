package id.ac.ui.cs.advprog.auth.dto;

import id.ac.ui.cs.advprog.auth.enums.UserRole;

public record UserResponse(
    Long id,
    String username,
    String email,
    String name,
    UserRole role
) {}
