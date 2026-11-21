package com.queuezero.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.queueZeroDataStore by preferencesDataStore(name = "queuezero_prefs")

/** Keeps the login token on the device, encrypted. */
class TokenStore(private val context: Context, private val cipher: TokenCipher) {

    private val tokenKey = stringPreferencesKey("access_token_encrypted")

    /** Emits the token, or null when logged out (or when it cannot be decrypted). */
    val token: Flow<String?> = context.queueZeroDataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs -> prefs[tokenKey]?.let { cipher.decrypt(it) } }

    suspend fun save(token: String) {
        context.queueZeroDataStore.edit { it[tokenKey] = cipher.encrypt(token) }
    }

    suspend fun clear() {
        context.queueZeroDataStore.edit { it.remove(tokenKey) }
    }

    /** Used by the network interceptor, which already runs on a background thread. */
    fun currentTokenBlocking(): String? = runBlocking { token.first() }
}
