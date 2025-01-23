package com.example.quizapp.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizapp.domain.model.QuizSummary
import com.example.quizapp.domain.usecase.auth.LogoutUseCase
import com.example.quizapp.domain.usecase.home.GetHomeContentUseCase
import com.example.quizapp.presentation.common.isAuthError
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

data class MainUiState(
    val isLoading: Boolean = false,
    val userLogin: String = "",
    val userExp: String = "",
    val isAdmin: Boolean = false,
    val quizzes: List<QuizSummary> = emptyList(),
)

sealed interface MainEvent {
    data class Message(val message: String) : MainEvent
    object NavigateToLogin : MainEvent
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getHomeContentUseCase: GetHomeContentUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = mutableUiState.asStateFlow()

    private val mutableEvents = MutableSharedFlow<MainEvent>()
    val events: SharedFlow<MainEvent> = mutableEvents.asSharedFlow()

    fun loadContent() {
        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoading = true) }
            runCatching { getHomeContentUseCase() }
                .onSuccess { content ->
                    mutableUiState.update {
                        it.copy(
                            isLoading = false,
                            userLogin = content.user.login,
                            userExp = content.user.exp,
                            isAdmin = content.user.role == "admin",
                            quizzes = content.quizzes,
                        )
                    }
                }
                .onFailure { throwable ->
                    mutableUiState.update { it.copy(isLoading = false) }
                    if (throwable.isAuthError()) {
                        viewModelScope.launch {
                            logoutUseCase()
                            mutableEvents.emit(MainEvent.NavigateToLogin)
                        }
                    } else {
                        viewModelScope.launch {
                            mutableEvents.emit(
                                MainEvent.Message(
                                    throwable.toUserMessage("Unable to load quizzes"),
                                ),
                            )
                        }
                    }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            mutableEvents.emit(MainEvent.NavigateToLogin)
        }
    }
}
