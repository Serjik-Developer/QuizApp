package com.example.quizapp.domain.usecase.admin

import com.example.quizapp.domain.model.AdminAnswer
import com.example.quizapp.domain.model.AnswerDraft
import com.example.quizapp.domain.repository.AdminRepository
import javax.inject.Inject

class GetAdminAnswersUseCase @Inject constructor(
    private val repository: AdminRepository,
) {
    suspend operator fun invoke(questionId: String): List<AdminAnswer> = repository.getAnswers(questionId)
}

class CreateAnswerUseCase @Inject constructor(
    private val repository: AdminRepository,
) {
    suspend operator fun invoke(questionId: String, draft: AnswerDraft) {
        repository.createAnswer(questionId, draft)
    }
}

class UpdateAnswerUseCase @Inject constructor(
    private val repository: AdminRepository,
) {
    suspend operator fun invoke(answerId: String, draft: AnswerDraft) {
        repository.updateAnswer(answerId, draft)
    }
}

class DeleteAnswerUseCase @Inject constructor(
    private val repository: AdminRepository,
) {
    suspend operator fun invoke(answerId: String) {
        repository.deleteAnswer(answerId)
    }
}
