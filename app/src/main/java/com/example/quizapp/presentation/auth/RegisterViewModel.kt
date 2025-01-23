package com.example.quizapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizapp.domain.usecase.auth.RegisterUseCase
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

sealed interface RegisterEvent {
    data class Error(val message: String) : RegisterEvent
    object NavigateToHome : RegisterEvent
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = mutableUiState.asStateFlow()

    private val mutableEvents = MutableSharedFlow<RegisterEvent>()
    val events: SharedFlow<RegisterEvent> = mutableEvents.asSharedFlow()

    fun register(login: String, password: String) {
        if (login.isBlank() || password.isBlank()) {
            emitError("Login and password must not be empty")
            return
        }

        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoading = true) }
            try {
                registerUseCase(login.trim(), password)
                mutableEvents.emit(RegisterEvent.NavigateToHome)
            } catch (throwable: Throwable) {
                emitError(throwable.toUserMessage("Unable to sign up"))
            }
            mutableUiState.update { it.copy(isLoading = false) }
        }
    }

    private fun emitError(message: String) {
        viewModelScope.launch {
            mutableEvents.emit(RegisterEvent.Error(message))
        }
    }
}
