package com.example.quizapp.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizapp.domain.model.QuizDraft
import com.example.quizapp.domain.model.QuizSummary
import com.example.quizapp.domain.usecase.admin.CreateQuizUseCase
import com.example.quizapp.domain.usecase.admin.DeleteQuizUseCase
import com.example.quizapp.domain.usecase.admin.GetAdminQuizzesUseCase
import com.example.quizapp.domain.usecase.admin.UpdateQuizUseCase
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

data class AdminQuizzesUiState(
    val isLoading: Boolean = false,
    val quizzes: List<QuizSummary> = emptyList(),
)

sealed interface AdminEvent {
    data class Message(val message: String) : AdminEvent
    object NavigateToLogin : AdminEvent
}

@HiltViewModel
class AdminQuizzesViewModel @Inject constructor(
    private val getAdminQuizzesUseCase: GetAdminQuizzesUseCase,
    private val createQuizUseCase: CreateQuizUseCase,
    private val updateQuizUseCase: UpdateQuizUseCase,
    private val deleteQuizUseCase: DeleteQuizUseCase,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(AdminQuizzesUiState())
    val uiState: StateFlow<AdminQuizzesUiState> = mutableUiState.asStateFlow()

    private val mutableEvents = MutableSharedFlow<AdminEvent>()
    val events: SharedFlow<AdminEvent> = mutableEvents.asSharedFlow()

    fun loadQuizzes() {
        executeWithRefresh(
            block = { getAdminQuizzesUseCase() },
            onSuccess = { quizzes -> mutableUiState.update { it.copy(isLoading = false, quizzes = quizzes) } },
        )
    }

    fun createQuiz(text: String, description: String) {
        if (text.isBlank()) {
            emitMessage("Quiz name must not be empty")
            return
        }
        launchMutation {
            createQuizUseCase(QuizDraft(text = text.trim(), description = description.trim()))
            loadQuizzes()
        }
    }

    fun updateQuiz(quizId: String, text: String, description: String) {
        if (text.isBlank()) {
            emitMessage("Quiz name must not be empty")
            return
        }
        launchMutation {
            updateQuizUseCase(quizId, QuizDraft(text = text.trim(), description = description.trim()))
            loadQuizzes()
        }
    }

    fun deleteQuiz(quizId: String) {
        launchMutation {
            deleteQuizUseCase(quizId)
            loadQuizzes()
        }
    }

    private fun launchMutation(block: suspend () -> Unit) {
        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoading = true) }
            try {
                block()
            } catch (throwable: Throwable) {
                handleFailure(throwable)
            }
        }
    }

    private fun executeWithRefresh(
        block: suspend () -> List<QuizSummary>,
        onSuccess: (List<QuizSummary>) -> Unit,
    ) {
        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoading = true) }
            try {
                onSuccess(block())
            } catch (throwable: Throwable) {
                handleFailure(throwable)
            }
        }
    }

    private fun handleFailure(throwable: Throwable) {
        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoading = false) }
            if (throwable.isAuthError()) {
                mutableEvents.emit(AdminEvent.NavigateToLogin)
            } else {
                mutableEvents.emit(AdminEvent.Message(throwable.toUserMessage("Admin request failed")))
            }
        }
    }

    private fun emitMessage(message: String) {
        viewModelScope.launch {
            mutableEvents.emit(AdminEvent.Message(message))
        }
    }
}
