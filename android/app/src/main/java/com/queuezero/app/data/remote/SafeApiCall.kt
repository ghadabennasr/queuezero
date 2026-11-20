package com.queuezero.app.data.remote

import com.queuezero.app.data.AppError
import com.queuezero.app.data.AppResult
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException

/** Runs a Retrofit call and turns every failure into a typed AppResult. */
suspend fun <T> safeApiCall(json: Json, block: suspend () -> T): AppResult<T> {
    return try {
        AppResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        AppResult.Failure(AppError.NoNetwork)
    } catch (e: HttpException) {
        AppResult.Failure(parseHttpError(e, json))
    } catch (e: Exception) {
        AppResult.Failure(AppError.Unknown(e.message))
    }
}

private fun parseHttpError(e: HttpException, json: Json): AppError.Api {
    val body = try {
        e.response()?.errorBody()?.string()
    } catch (ignored: Exception) {
        null
    }
    val parsed = body?.let {
        try {
            json.decodeFromString<ApiErrorEnvelope>(it).error
        } catch (ignored: Exception) {
            null
        }
    }
    return AppError.Api(
        httpCode = e.code(),
        code = parsed?.code,
        message = parsed?.message ?: "Server error (${e.code()})"
    )
}
