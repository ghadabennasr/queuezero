package com.queuezero.app.ui.common

import com.queuezero.app.data.AppError

/** Turns a technical error into text the user can read. */
fun AppError.toUserMessage(): String = when (this) {
    AppError.NoNetwork -> "Can't reach the server. Check your connection and that the backend is running."
    is AppError.Api -> message
    is AppError.Unknown -> "Something went wrong. Please try again."
}
