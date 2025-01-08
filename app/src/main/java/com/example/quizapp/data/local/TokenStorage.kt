package com.example.quizapp.data.local

import kotlinx.coroutines.flow.StateFlow

interface TokenStorage {
    val tokenFlow: StateFlow<String?>

    suspend fun saveToken(token: String)

    suspend fun clearToken()

    fun currentToken(): String?
}
