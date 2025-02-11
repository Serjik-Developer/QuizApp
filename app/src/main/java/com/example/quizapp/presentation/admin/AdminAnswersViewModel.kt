package com.example.quizapp.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizapp.domain.model.AdminAnswer
import com.example.quizapp.domain.model.AnswerDraft
import com.example.quizapp.domain.usecase.admin.CreateAnswerUseCase
import com.example.quizapp.domain.usecase.admin.DeleteAnswerUseCase
import com.example.quizapp.domain.usecase.admin.GetAdminAnswersUseCase
import com.example.quizapp.domain.usecase.admin.UpdateAnswerUseCase
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

data class AdminAnswersUiState(
    val isLoading: Boolean = false,
    val answers: List<AdminAnswer> = emptyList(),
)

@HiltViewModel
class AdminAnswersViewModel @Inject constructor(
    private val getAdminAnswersUseCase: GetAdminAnswersUseCase,
    private val createAnswerUseCase: CreateAnswerUseCase,
    private val updateAnswerUseCase: UpdateAnswerUseCase,
    private val deleteAnswerUseCase: DeleteAnswerUseCase,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(AdminAnswersUiState())
    val uiState: StateFlow<AdminAnswersUiState> = mutableUiState.asStateFlow()

    private val mutableEvents = MutableSharedFlow<AdminEvent>()
    val events: SharedFlow<AdminEvent> = mutableEvents.asSharedFlow()

    private var questionId: String? = null

    fun loadAnswers(targetQuestionId: String) {
        questionId = targetQuestionId
        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoading = true) }
            try {
                val answers = getAdminAnswersUseCase(targetQuestionId)
                mutableUiState.update { it.copy(isLoading = false, answers = answers) }
            } catch (throwable: Throwable) {
                handleFailure(throwable)
            }
        }
    }

    fun createAnswer(text: String, explanation: String, trueValue: String) {
        val currentQuestionId = questionId ?: return
        if (text.isBlank()) {
            emitMessage("Answer text is required")
            return
        }
        launchMutation {
            createAnswerUseCase(
                currentQuestionId,
                AnswerDraft(
                    text = text.trim(),
                    explanation = explanation.trim(),
                    correctValue = trueValue.trim(),
                ),
            )
            loadAnswers(currentQuestionId)
        }
    }

    fun updateAnswer(answerId: String, text: String, explanation: String, trueValue: String) {
        if (text.isBlank()) {
            emitMessage("Answer text is required")
            return
        }
        launchMutation {
            updateAnswerUseCase(
                answerId,
                AnswerDraft(
                    text = text.trim(),
                    explanation = explanation.trim(),
                    correctValue = trueValue.trim(),
                ),
            )
            questionId?.let(::loadAnswers)
        }
    }

    fun deleteAnswer(answerId: String) {
        launchMutation {
            deleteAnswerUseCase(answerId)
            questionId?.let(::loadAnswers)
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
                mutableEvents.emit(AdminEvent.Message(throwable.toUserMessage("Answer request failed")))
            }
        }
    }

    private fun emitMessage(message: String) {
        viewModelScope.launch {
            mutableEvents.emit(AdminEvent.Message(message))
        }
    }
}
