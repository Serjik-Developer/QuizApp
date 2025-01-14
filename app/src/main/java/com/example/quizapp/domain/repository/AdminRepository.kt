package com.example.quizapp.domain.repository

import com.example.quizapp.domain.model.AdminAnswer
import com.example.quizapp.domain.model.AdminQuestion
import com.example.quizapp.domain.model.AnswerDraft
import com.example.quizapp.domain.model.QuestionDraft
import com.example.quizapp.domain.model.QuizDraft

interface AdminRepository {
    suspend fun getQuizzes(): List<com.example.quizapp.domain.model.QuizSummary>

    suspend fun createQuiz(draft: QuizDraft)

    suspend fun updateQuiz(quizId: String, draft: QuizDraft)

    suspend fun deleteQuiz(quizId: String)

    suspend fun getQuestions(quizId: String): List<AdminQuestion>

    suspend fun createQuestion(quizId: String, draft: QuestionDraft)

    suspend fun updateQuestion(questionId: String, question: String)

    suspend fun deleteQuestion(questionId: String)

    suspend fun getAnswers(questionId: String): List<AdminAnswer>

    suspend fun createAnswer(questionId: String, draft: AnswerDraft)

    suspend fun updateAnswer(answerId: String, draft: AnswerDraft)

    suspend fun deleteAnswer(answerId: String)
}
