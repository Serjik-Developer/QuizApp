package com.example.quizapp.domain.usecase.auth

import com.example.quizapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(login: String, password: String) {
        repository.login(login, password)
    }
}

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(login: String, password: String) {
        repository.register(login, password)
    }
}

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke() {
        repository.logout()
    }
}

class ObserveTokenUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    operator fun invoke(): Flow<String?> = repository.observeToken()
}
