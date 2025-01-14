package com.example.quizapp.domain.usecase.quiz

import com.example.quizapp.domain.model.AnswerEvaluation
import com.example.quizapp.domain.model.QuestionDetail
import com.example.quizapp.domain.repository.QuizRepository
import javax.inject.Inject

class GetQuizQuestionIdsUseCase @Inject constructor(
    private val repository: QuizRepository,
) {
    suspend operator fun invoke(quizId: String): List<String> = repository.getQuizQuestionIds(quizId)
}

class GetQuestionDetailUseCase @Inject constructor(
    private val repository: QuizRepository,
) {
    suspend operator fun invoke(questionId: String): QuestionDetail = repository.getQuestionDetail(questionId)
}

class SubmitAnswerUseCase @Inject constructor(
    private val repository: QuizRepository,
) {
    suspend operator fun invoke(answerId: String, answer: String): AnswerEvaluation {
        return repository.submitAnswer(answerId, answer)
    }
}
