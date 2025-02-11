package com.example.quizapp.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizapp.domain.model.AdminQuestion
import com.example.quizapp.domain.model.QuestionDraft
import com.example.quizapp.domain.usecase.admin.CreateQuestionUseCase
import com.example.quizapp.domain.usecase.admin.DeleteQuestionUseCase
import com.example.quizapp.domain.usecase.admin.GetAdminQuestionsUseCase
import com.example.quizapp.domain.usecase.admin.UpdateQuestionUseCase
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

data class AdminQuestionsUiState(
    val isLoading: Boolean = false,
    val questions: List<AdminQuestion> = emptyList(),
)

@HiltViewModel
class AdminQuestionsViewModel @Inject constructor(
    private val getAdminQuestionsUseCase: GetAdminQuestionsUseCase,
    private val createQuestionUseCase: CreateQuestionUseCase,
    private val updateQuestionUseCase: UpdateQuestionUseCase,
    private val deleteQuestionUseCase: DeleteQuestionUseCase,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(AdminQuestionsUiState())
    val uiState: StateFlow<AdminQuestionsUiState> = mutableUiState.asStateFlow()

    private val mutableEvents = MutableSharedFlow<AdminEvent>()
    val events: SharedFlow<AdminEvent> = mutableEvents.asSharedFlow()

    private var quizId: String? = null

    fun loadQuestions(targetQuizId: String) {
        quizId = targetQuizId
        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoading = true) }
            try {
                val questions = getAdminQuestionsUseCase(targetQuizId)
                mutableUiState.update { it.copy(isLoading = false, questions = questions) }
            } catch (throwable: Throwable) {
                handleFailure(throwable)
            }
        }
    }

    fun createQuestion(question: String, type: String) {
        val currentQuizId = quizId ?: return
        if (question.isBlank() || type.isBlank()) {
            emitMessage("Question text and type are required")
            return
        }
        launchMutation {
            createQuestionUseCase(currentQuizId, QuestionDraft(question = question.trim(), type = type.trim()))
            loadQuestions(currentQuizId)
        }
    }

    fun updateQuestion(questionId: String, question: String) {
        if (question.isBlank()) {
            emitMessage("Question text is required")
            return
        }
        launchMutation {
            updateQuestionUseCase(questionId, question.trim())
            quizId?.let(::loadQuestions)
        }
    }

    fun deleteQuestion(questionId: String) {
        launchMutation {
            deleteQuestionUseCase(questionId)
            quizId?.let(::loadQuestions)
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

    private fun handleFailure(throwable: Throwable) {
        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoading = false) }
            if (throwable.isAuthError()) {
                mutableEvents.emit(AdminEvent.NavigateToLogin)
            } else {
                mutableEvents.emit(AdminEvent.Message(throwable.toUserMessage("Question request failed")))
            }
        }
    }

    private fun emitMessage(message: String) {
        viewModelScope.launch {
            mutableEvents.emit(AdminEvent.Message(message))
        }
    }
}
