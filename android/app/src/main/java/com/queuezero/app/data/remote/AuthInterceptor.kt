package com.queuezero.app.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/** Adds "Authorization: Bearer <token>" to every call except login and register. */
class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        if (path.endsWith("/api/auth/login") || path.endsWith("/api/auth/register")) {
            return chain.proceed(request)
        }
        val token = tokenProvider() ?: return chain.proceed(request)
        return chain.proceed(
            request.newBuilder().header("Authorization", "Bearer $token").build()
        )
    }
}
