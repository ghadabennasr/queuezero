package com.queuezero.app.data

/** What a call can fail with. The UI turns these into messages. */
sealed interface AppError {
    data object NoNetwork : AppError
    data class Api(val httpCode: Int, val code: String?, val message: String) : AppError
    data class Unknown(val message: String?) : AppError
}

/** Every repository call returns one of these instead of throwing. */
sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}
