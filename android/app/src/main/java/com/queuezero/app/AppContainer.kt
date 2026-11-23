package com.queuezero.app

import android.content.Context
import com.queuezero.app.data.local.TokenCipher
import com.queuezero.app.data.local.TokenStore
import com.queuezero.app.data.remote.ApiConfig
import com.queuezero.app.data.remote.AuthApi
import com.queuezero.app.data.remote.AuthInterceptor
import com.queuezero.app.data.remote.LocationApi
import com.queuezero.app.data.repository.AuthRepository
import com.queuezero.app.data.repository.LocationRepository
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Builds the app's long-lived objects once.
 * (A hand-written stand-in for dependency injection; Hilt replaces it in a later step.)
 */
class AppContainer(context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val tokenStore = TokenStore(context.applicationContext, TokenCipher())

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor { tokenStore.currentTokenBlocking() })
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val authRepository = AuthRepository(retrofit.create(AuthApi::class.java), tokenStore, json)

    val locationRepository = LocationRepository(retrofit.create(LocationApi::class.java), json)
}
