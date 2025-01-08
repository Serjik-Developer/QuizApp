package com.example.quizapp.data.remote

import org.json.JSONObject

fun String.extractApiMessage(defaultMessage: String): String {
    if (isBlank()) return defaultMessage
    return runCatching {
        val json = JSONObject(this)
        json.optString("message").ifBlank { defaultMessage }
    }.getOrDefault(defaultMessage)
}
