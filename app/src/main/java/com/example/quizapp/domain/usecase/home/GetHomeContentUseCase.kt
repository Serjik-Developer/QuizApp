package com.example.quizapp.domain.usecase.home

import com.example.quizapp.domain.model.QuizSummary
import com.example.quizapp.domain.model.UserProfile
import com.example.quizapp.domain.repository.QuizRepository
import javax.inject.Inject

data class HomeContent(
    val user: UserProfile,
    val quizzes: List<QuizSummary>,
)

class GetHomeContentUseCase @Inject constructor(
    private val repository: QuizRepository,
) {
    suspend operator fun invoke(): HomeContent {
        val user = repository.getCurrentUser()
        val quizzes = repository.getQuizzes()
        return HomeContent(user = user, quizzes = quizzes)
    }
}
