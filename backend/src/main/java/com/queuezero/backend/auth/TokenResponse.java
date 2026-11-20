package com.queuezero.backend.auth;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
}
