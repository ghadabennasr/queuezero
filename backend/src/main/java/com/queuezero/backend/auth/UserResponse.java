package com.queuezero.backend.auth;

public record UserResponse(Long id, String email, String fullName, String role) {

    static UserResponse from(AppUser u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRole());
    }
}
