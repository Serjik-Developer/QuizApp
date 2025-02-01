package com.example.quizapp.presentation.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizapp.domain.usecase.quiz.GetQuizQuestionIdsUseCase
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

data class QuizOverviewUiState(
    val isLoading: Boolean = false,
    val questionIds: List<String> = emptyList(),
    val questionCountLabel: String = "",
)

sealed interface QuizOverviewEvent {
    data class Message(val message: String) : QuizOverviewEvent
    object NavigateToLogin : QuizOverviewEvent
}

@HiltViewModel
class QuizOverviewViewModel @Inject constructor(
    private val getQuizQuestionIdsUseCase: GetQuizQuestionIdsUseCase,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(QuizOverviewUiState())
    val uiState: StateFlow<QuizOverviewUiState> = mutableUiState.asStateFlow()

    private val mutableEvents = MutableSharedFlow<QuizOverviewEvent>()
    val events: SharedFlow<QuizOverviewEvent> = mutableEvents.asSharedFlow()

    fun loadQuiz(quizId: String) {
        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoading = true) }
            try {
                val questionIds = getQuizQuestionIdsUseCase(quizId)
                mutableUiState.update {
                    it.copy(
                        isLoading = false,
                        questionIds = questionIds,
                        questionCountLabel = formatQuestionCount(questionIds.size),
                    )
                }
            } catch (throwable: Throwable) {
                mutableUiState.update { it.copy(isLoading = false) }
                if (throwable.isAuthError()) {
                    mutableEvents.emit(QuizOverviewEvent.NavigateToLogin)
                } else {
                    mutableEvents.emit(
                        QuizOverviewEvent.Message(
                            throwable.toUserMessage("Unable to load quiz questions"),
                        ),
                    )
                }
            }
        }
    }

    private fun formatQuestionCount(count: Int): String {
        return "Questions: $count"
    }
}
