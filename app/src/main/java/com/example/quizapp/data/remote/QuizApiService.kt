package com.example.quizapp.data.remote

import com.example.quizapp.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizApiService @Inject constructor(
    private val client: OkHttpClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    suspend fun get(path: String, token: String? = null): String = execute(
        requestBuilder(path, token).get().build(),
    )

    suspend fun post(path: String, body: JSONObject, token: String? = null): String = execute(
        requestBuilder(path, token)
            .post(body.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build(),
    )

    suspend fun put(path: String, body: JSONObject, token: String): String = execute(
        requestBuilder(path, token)
            .put(body.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build(),
    )

    suspend fun delete(path: String, token: String): String = execute(
        requestBuilder(path, token).delete().build(),
    )

    suspend fun getJsonObject(path: String, token: String? = null): JSONObject =
        JSONObject(get(path, token))

    suspend fun getJsonArray(path: String, token: String? = null): JSONArray =
        JSONArray(get(path, token))

    private suspend fun execute(request: Request): String = withContext(ioDispatcher) {
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw ApiException(
                    message = body.extractApiMessage("Request failed with code ${response.code}"),
                    code = response.code,
                )
            }
            body
        }
    }

    private fun requestBuilder(path: String, token: String?): Request.Builder {
        val builder = Request.Builder()
            .url("$BASE_URL/$path")

        if (!token.isNullOrBlank()) {
            builder.addHeader("Authorization", "Bearer $token")
        }

        return builder
    }

    private companion object {
        const val BASE_URL = "https://backend-quizapp-9ad6.onrender.com"
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
