package com.example.quizapp.data.repository

import com.example.quizapp.data.local.TokenStorage
import com.example.quizapp.data.remote.ApiException
import com.example.quizapp.data.remote.QuizApiService
import com.example.quizapp.domain.model.AnswerEvaluation
import com.example.quizapp.domain.model.AnswerOption
import com.example.quizapp.domain.model.QuestionDetail
import com.example.quizapp.domain.model.QuizSummary
import com.example.quizapp.domain.model.UserProfile
import com.example.quizapp.domain.repository.QuizRepository
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepositoryImpl @Inject constructor(
    private val apiService: QuizApiService,
    private val tokenStorage: TokenStorage,
) : QuizRepository {

    override suspend fun getCurrentUser(): UserProfile {
        val token = requireToken()
        val response = apiService.getJsonObject(path = "user", token = token)
        return UserProfile(
            login = response.getString("login"),
            exp = response.getString("exp"),
            role = response.getString("role"),
        )
    }

    override suspend fun getQuizzes(): List<QuizSummary> {
        val token = requireToken()
        val response = apiService.getJsonArray(path = "Quiz", token = token)
        return response.toQuizSummaries()
    }

    override suspend fun getQuizQuestionIds(quizId: String): List<String> {
        val token = requireToken()
        val response = apiService.getJsonArray(path = "QuizQustionsForUser/$quizId", token = token)
        return buildList {
            for (index in 0 until response.length()) {
                add(response.getJSONObject(index).getString("qid"))
            }
        }
    }

    override suspend fun getQuestionDetail(questionId: String): QuestionDetail {
        val token = requireToken()
        val response = apiService.getJsonObject(path = "QuizQustionForUser/$questionId", token = token)
        val answersValue = response.get("answers")
        val answers = mutableListOf<AnswerOption>()

        when (answersValue) {
            is JSONArray -> {
                for (index in 0 until answersValue.length()) {
                    answers += answersValue.getJSONObject(index).toAnswerOption()
                }
            }
            is JSONObject -> {
                answers += answersValue.toAnswerOption()
            }
        }

        return QuestionDetail(
            text = response.getString("textQ"),
            type = response.getString("type"),
            answers = answers,
        )
    }

    override suspend fun submitAnswer(answerId: String, answer: String): AnswerEvaluation {
        val token = requireToken()
        val body = JSONObject().put("AnswerUser", answer)
        val response = apiService.post(path = "QuizAnswerForUser/$answerId", body = body, token = token)
        val json = JSONObject(response)
        return AnswerEvaluation(
            status = json.getString("status"),
            explanation = json.optString("explanation"),
            correct = json.optString("correct"),
            your = json.optString("your"),
        )
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

    private fun JSONObject.toAnswerOption(): AnswerOption = AnswerOption(
        text = getString("text"),
        aid = getString("aid"),
    )
}
