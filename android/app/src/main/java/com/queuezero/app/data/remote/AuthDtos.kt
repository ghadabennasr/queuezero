package com.queuezero.app.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RegisterRequest(val email: String, val fullName: String, val password: String)

@Serializable
data class TokenResponse(val accessToken: String, val tokenType: String, val expiresIn: Long)

@Serializable
data class UserResponse(val id: Long, val email: String, val fullName: String, val role: String)

/** The backend's error format: {"error":{"code":"...","message":"..."}} */
@Serializable
data class ApiErrorEnvelope(val error: ApiErrorBody)

@Serializable
data class ApiErrorBody(val code: String, val message: String)
