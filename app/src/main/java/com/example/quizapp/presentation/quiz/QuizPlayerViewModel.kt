package com.example.quizapp.presentation.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizapp.domain.model.AnswerOption
import com.example.quizapp.domain.usecase.quiz.GetQuestionDetailUseCase
import com.example.quizapp.domain.usecase.quiz.SubmitAnswerUseCase
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

data class QuizPlayerUiState(
    val isLoading: Boolean = false,
    val questionText: String = "",
    val questionType: String = "",
    val answers: List<AnswerOption> = emptyList(),
    val explanation: String = "",
    val resultMessage: String = "",
    val hasSubmitted: Boolean = false,
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 0,
    val totalExp: Int = 0,
)

sealed interface QuizPlayerEvent {
    data class Message(val message: String) : QuizPlayerEvent
    data class Finished(val exp: Int) : QuizPlayerEvent
    object NavigateToLogin : QuizPlayerEvent
}

@HiltViewModel
class QuizPlayerViewModel @Inject constructor(
    private val getQuestionDetailUseCase: GetQuestionDetailUseCase,
    private val submitAnswerUseCase: SubmitAnswerUseCase,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(QuizPlayerUiState())
    val uiState: StateFlow<QuizPlayerUiState> = mutableUiState.asStateFlow()

    private val mutableEvents = MutableSharedFlow<QuizPlayerEvent>()
    val events: SharedFlow<QuizPlayerEvent> = mutableEvents.asSharedFlow()

    private var questionIds: List<String> = emptyList()
    private var currentQuestionPosition = 0
    private var sessionStarted = false

    fun startSession(ids: List<String>) {
        if (sessionStarted) return
        sessionStarted = true
        questionIds = ids

        if (questionIds.isEmpty()) {
            viewModelScope.launch {
                mutableEvents.emit(QuizPlayerEvent.Message("Quiz has no questions yet"))
                mutableEvents.emit(QuizPlayerEvent.Finished(0))
            }
            return
        }

        mutableUiState.update {
            it.copy(
                totalQuestions = questionIds.size,
                currentQuestionIndex = 1,
            )
        }
        loadCurrentQuestion()
    }

    fun submitAnswer(answer: String) {
        val state = mutableUiState.value
        if (state.hasSubmitted) {
            emitMessage("You already answered this question")
            return
        }
        if (answer.isBlank()) {
            emitMessage("Enter or select an answer first")
            return
        }

        val answerId = state.answers.firstOrNull()?.aid
        if (answerId.isNullOrBlank()) {
            emitMessage("This question does not contain an answer key")
            return
        }

        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoading = true) }
            try {
                val evaluation = submitAnswerUseCase(answerId, answer.trim())
                val updatedExp = if (evaluation.status == "GOOD") {
                    mutableUiState.value.totalExp + 1
                } else {
                    mutableUiState.value.totalExp
                }
                mutableUiState.update {
                    it.copy(
                        isLoading = false,
                        hasSubmitted = true,
                        explanation = evaluation.explanation,
                        resultMessage = when (evaluation.status) {
                            "GOOD" -> "Correct answer"
                            "BAD" -> "Wrong answer. Correct: ${evaluation.correct}"
                            else -> evaluation.status
                        },
                        totalExp = updatedExp,
                    )
                }
            } catch (throwable: Throwable) {
                mutableUiState.update { it.copy(isLoading = false) }
                if (throwable.isAuthError()) {
                    mutableEvents.emit(QuizPlayerEvent.NavigateToLogin)
                } else {
                    mutableEvents.emit(
                        QuizPlayerEvent.Message(
                            throwable.toUserMessage("Unable to validate answer"),
                        ),
                    )
                }
            }
        }
    }

    fun nextQuestion() {
        if (questionIds.isEmpty()) {
            emitMessage("Quiz has no questions")
            return
        }

        if (currentQuestionPosition >= questionIds.lastIndex) {
            viewModelScope.launch {
                mutableEvents.emit(QuizPlayerEvent.Finished(mutableUiState.value.totalExp))
            }
            return
        }

        currentQuestionPosition += 1
        mutableUiState.update {
            it.copy(currentQuestionIndex = currentQuestionPosition + 1)
        }
        loadCurrentQuestion()
    }

    private fun loadCurrentQuestion() {
        val questionId = questionIds.getOrNull(currentQuestionPosition) ?: return
        viewModelScope.launch {
            mutableUiState.update {
                it.copy(
                    isLoading = true,
                    hasSubmitted = false,
                    explanation = "",
                    resultMessage = "",
                )
            }
            try {
                val question = getQuestionDetailUseCase(questionId)
                mutableUiState.update {
                    it.copy(
                        isLoading = false,
                        questionText = question.text,
                        questionType = question.type,
                        answers = question.answers,
                    )
                }
            } catch (throwable: Throwable) {
                mutableUiState.update { it.copy(isLoading = false) }
                if (throwable.isAuthError()) {
                    mutableEvents.emit(QuizPlayerEvent.NavigateToLogin)
                } else {
                    mutableEvents.emit(
                        QuizPlayerEvent.Message(
                            throwable.toUserMessage("Unable to load question"),
                        ),
                    )
                }
            }
        }
    }

    private fun emitMessage(message: String) {
        viewModelScope.launch {
            mutableEvents.emit(QuizPlayerEvent.Message(message))
        }
    }
}
