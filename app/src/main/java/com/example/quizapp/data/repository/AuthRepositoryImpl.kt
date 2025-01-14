package com.example.quizapp.data.repository

import com.example.quizapp.data.local.TokenStorage
import com.example.quizapp.data.remote.QuizApiService
import com.example.quizapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: QuizApiService,
    private val tokenStorage: TokenStorage,
) : AuthRepository {

    override fun observeToken(): Flow<String?> = tokenStorage.tokenFlow

    override fun currentToken(): String? = tokenStorage.currentToken()

    override suspend fun login(login: String, password: String) {
        val body = JSONObject()
            .put("login", login)
            .put("password", password)

        val response = apiService.post(path = "auth", body = body)
        val token = JSONObject(response).getString("token")
        tokenStorage.saveToken(token)
    }

    override suspend fun register(login: String, password: String) {
        val body = JSONObject()
            .put("login", login)
            .put("password", password)

        val response = apiService.post(path = "register", body = body)
        val token = JSONObject(response).getString("token")
        tokenStorage.saveToken(token)
    }

    override suspend fun logout() {
        tokenStorage.clearToken()
    }
}
