package com.example.quizapp.domain.usecase.admin

import com.example.quizapp.domain.model.AdminQuestion
import com.example.quizapp.domain.model.QuestionDraft
import com.example.quizapp.domain.repository.AdminRepository
import javax.inject.Inject

class GetAdminQuestionsUseCase @Inject constructor(
    private val repository: AdminRepository,
) {
    suspend operator fun invoke(quizId: String): List<AdminQuestion> = repository.getQuestions(quizId)
}

class CreateQuestionUseCase @Inject constructor(
    private val repository: AdminRepository,
) {
    suspend operator fun invoke(quizId: String, draft: QuestionDraft) {
        repository.createQuestion(quizId, draft)
    }
}

class UpdateQuestionUseCase @Inject constructor(
    private val repository: AdminRepository,
) {
    suspend operator fun invoke(questionId: String, question: String) {
        repository.updateQuestion(questionId, question)
    }
}

class DeleteQuestionUseCase @Inject constructor(
    private val repository: AdminRepository,
) {
    suspend operator fun invoke(questionId: String) {
        repository.deleteQuestion(questionId)
    }
}
