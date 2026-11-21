package com.queuezero.app.data.repository

import com.queuezero.app.data.AppError
import com.queuezero.app.data.AppResult
import com.queuezero.app.data.local.TokenStore
import com.queuezero.app.data.remote.AuthApi
import com.queuezero.app.data.remote.LoginRequest
import com.queuezero.app.data.remote.RegisterRequest
import com.queuezero.app.data.remote.UserResponse
import com.queuezero.app.data.remote.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class AuthRepository(
    private val api: AuthApi,
    private val tokenStore: TokenStore,
    private val json: Json
) {

    /** True while a token is stored. The UI uses it to decide between Login and Home. */
    val isLoggedIn: Flow<Boolean> = tokenStore.token.map { it != null }

    suspend fun login(email: String, password: String): AppResult<Unit> {
        return when (val result = safeApiCall(json) { api.login(LoginRequest(email.trim(), password)) }) {
            is AppResult.Success -> {
                tokenStore.save(result.value.accessToken)
                AppResult.Success(Unit)
            }
            is AppResult.Failure -> result
        }
    }

    suspend fun register(email: String, fullName: String, password: String): AppResult<UserResponse> {
        return safeApiCall(json) { api.register(RegisterRequest(email.trim(), fullName.trim(), password)) }
    }

    /** Also logs the user out locally if the server says the token is no longer valid. */
    suspend fun me(): AppResult<UserResponse> {
        val result = safeApiCall(json) { api.me() }
        if (result is AppResult.Failure) {
            val error = result.error
            if (error is AppError.Api && error.httpCode == 401) {
                tokenStore.clear()
            }
        }
        return result
    }

    suspend fun logout() {
        tokenStore.clear()
    }
}
