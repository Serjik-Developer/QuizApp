package com.example.quizapp.domain.usecase.admin

import com.example.quizapp.domain.model.QuizDraft
import com.example.quizapp.domain.model.QuizSummary
import com.example.quizapp.domain.repository.AdminRepository
import javax.inject.Inject

class GetAdminQuizzesUseCase @Inject constructor(
    private val repository: AdminRepository,
) {
    suspend operator fun invoke(): List<QuizSummary> = repository.getQuizzes()
}

class CreateQuizUseCase @Inject constructor(
    private val repository: AdminRepository,
) {
    suspend operator fun invoke(draft: QuizDraft) {
        repository.createQuiz(draft)
    }
}

class UpdateQuizUseCase @Inject constructor(
    private val repository: AdminRepository,
) {
    suspend operator fun invoke(quizId: String, draft: QuizDraft) {
        repository.updateQuiz(quizId, draft)
    }
}

class DeleteQuizUseCase @Inject constructor(
    private val repository: AdminRepository,
) {
    suspend operator fun invoke(quizId: String) {
        repository.deleteQuiz(quizId)
    }
}
