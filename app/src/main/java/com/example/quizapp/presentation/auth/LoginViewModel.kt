package com.example.quizapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizapp.domain.usecase.auth.LoginUseCase
import com.example.quizapp.presentation.common.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
)

sealed interface LoginEvent {
    data class Error(val message: String) : LoginEvent
    object NavigateToHome : LoginEvent
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = mutableUiState.asStateFlow()

    private val mutableEvents = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = mutableEvents.asSharedFlow()

    fun login(login: String, password: String) {
        if (login.isBlank() || password.isBlank()) {
            emitError("Login and password must not be empty")
            return
        }

        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoading = true) }
            try {
                loginUseCase(login.trim(), password)
                mutableEvents.emit(LoginEvent.NavigateToHome)
            } catch (throwable: Throwable) {
                emitError(throwable.toUserMessage("Unable to sign in"))
            }
            mutableUiState.update { it.copy(isLoading = false) }
        }
    }

    private fun emitError(message: String) {
        viewModelScope.launch {
            mutableEvents.emit(LoginEvent.Error(message))
        }
    }
}
