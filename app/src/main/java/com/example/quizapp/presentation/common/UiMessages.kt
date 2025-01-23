package com.example.quizapp.presentation.common

import com.example.quizapp.data.remote.ApiException

fun Throwable.toUserMessage(defaultMessage: String): String {
    return when (this) {
        is ApiException -> message ?: defaultMessage
        else -> message ?: defaultMessage
    }
}

fun Throwable.isAuthError(): Boolean {
    return this is ApiException && (code == 401 || message?.contains("authorized", ignoreCase = true) == true)
}
