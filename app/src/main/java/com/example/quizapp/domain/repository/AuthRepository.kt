package com.example.quizapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeToken(): Flow<String?>

    fun currentToken(): String?

    suspend fun login(login: String, password: String)

    suspend fun register(login: String, password: String)

    suspend fun logout()
}
