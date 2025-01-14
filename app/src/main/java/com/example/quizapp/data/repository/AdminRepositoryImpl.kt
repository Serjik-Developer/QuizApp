package com.example.quizapp.data.repository

import com.example.quizapp.data.local.TokenStorage
import com.example.quizapp.data.remote.ApiException
import com.example.quizapp.data.remote.QuizApiService
import com.example.quizapp.domain.model.AdminAnswer
import com.example.quizapp.domain.model.AdminQuestion
import com.example.quizapp.domain.model.AnswerDraft
import com.example.quizapp.domain.model.QuestionDraft
import com.example.quizapp.domain.model.QuizDraft
import com.example.quizapp.domain.model.QuizSummary
import com.example.quizapp.domain.repository.AdminRepository
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val apiService: QuizApiService,
    private val tokenStorage: TokenStorage,
) : AdminRepository {

    override suspend fun getQuizzes(): List<QuizSummary> {
        val token = requireToken()
        val response = apiService.getJsonArray(path = "Quiz", token = token)
        return response.toQuizSummaries()
    }

    override suspend fun createQuiz(draft: QuizDraft) {
        val token = requireToken()
        val body = JSONObject()
            .put("Text", draft.text)
            .put("Description", draft.description)

        apiService.post(path = "Quiz", body = body, token = token)
    }

    override suspend fun updateQuiz(quizId: String, draft: QuizDraft) {
        val token = requireToken()
        val body = JSONObject()
            .put("Text", draft.text)
            .put("Description", draft.description)

        apiService.put(path = "Quiz/$quizId", body = body, token = token)
    }

    override suspend fun deleteQuiz(quizId: String) {
        val token = requireToken()
        apiService.delete(path = "Quiz/$quizId", token = token)
    }

    override suspend fun getQuestions(quizId: String): List<AdminQuestion> {
        val token = requireToken()
        val response = apiService.getJsonArray(path = "QuestionsAdmin/$quizId", token = token)
        return buildList {
            for (index in 0 until response.length()) {
                val item = response.getJSONObject(index)
                add(
                    AdminQuestion(
                        id = item.getString("id"),
                        qid = item.getString("qid"),
                        question = item.getString("question"),
                        type = item.getString("type"),
                    ),
                )
            }
        }
    }

    override suspend fun createQuestion(quizId: String, draft: QuestionDraft) {
        val token = requireToken()
        val body = JSONObject()
            .put("Question", draft.question)
            .put("Type", draft.type)

        apiService.post(path = "QuizQuestion/$quizId", body = body, token = token)
    }

    override suspend fun updateQuestion(questionId: String, question: String) {
        val token = requireToken()
        val body = JSONObject().put("question", question)
        apiService.put(path = "QuizQuestion/$questionId", body = body, token = token)
    }

    override suspend fun deleteQuestion(questionId: String) {
        val token = requireToken()
        apiService.delete(path = "QuizQuestion/$questionId", token = token)
    }

    override suspend fun getAnswers(questionId: String): List<AdminAnswer> {
        val token = requireToken()
        val response = apiService.getJsonArray(path = "AnswersAdmin/$questionId", token = token)
        return buildList {
            for (index in 0 until response.length()) {
                val item = response.getJSONObject(index)
                add(
                    AdminAnswer(
                        qid = item.getString("qid"),
                        aid = item.getString("aid"),
                        text = item.getString("text"),
                        explanation = item.getString("explanation"),
                        trueValue = item.getString("true"),
                    ),
                )
            }
        }
    }

    override suspend fun createAnswer(questionId: String, draft: AnswerDraft) {
        val token = requireToken()
        val body = JSONObject()
            .put("Text", draft.text)
            .put("Explanation", draft.explanation)
            .put("True", draft.correctValue)

        apiService.post(path = "QuizAnswer/$questionId", body = body, token = token)
    }

    override suspend fun updateAnswer(answerId: String, draft: AnswerDraft) {
        val token = requireToken()
        val body = JSONObject()
            .put("text", draft.text)
            .put("explanation", draft.explanation)
            .put("trueQustion", draft.correctValue)

        apiService.put(path = "QuizAnswer/$answerId", body = body, token = token)
    }

    override suspend fun deleteAnswer(answerId: String) {
        val token = requireToken()
        apiService.delete(path = "QuizAnswer/$answerId", token = token)
    }

    private fun requireToken(): String {
        return tokenStorage.currentToken()
            ?: throw ApiException("Authentication token is missing")
    }

    private fun JSONArray.toQuizSummaries(): List<QuizSummary> = buildList {
        for (index in 0 until length()) {
            val item = getJSONObject(index)
            add(
                QuizSummary(
                    id = item.getString("Id"),
                    name = item.getString("Text"),
                    description = item.getString("Description"),
                ),
            )
        }
    }
}
