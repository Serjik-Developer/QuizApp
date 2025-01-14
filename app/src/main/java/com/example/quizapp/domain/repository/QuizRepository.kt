package com.example.quizapp.domain.repository

import com.example.quizapp.domain.model.AnswerEvaluation
import com.example.quizapp.domain.model.QuestionDetail
import com.example.quizapp.domain.model.QuizSummary
import com.example.quizapp.domain.model.UserProfile

interface QuizRepository {
    suspend fun getCurrentUser(): UserProfile

    suspend fun getQuizzes(): List<QuizSummary>

    suspend fun getQuizQuestionIds(quizId: String): List<String>

    suspend fun getQuestionDetail(questionId: String): QuestionDetail

    suspend fun submitAnswer(answerId: String, answer: String): AnswerEvaluation
}
