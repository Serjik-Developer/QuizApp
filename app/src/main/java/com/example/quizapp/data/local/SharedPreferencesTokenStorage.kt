package com.example.quizapp.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesTokenStorage @Inject constructor(
    @ApplicationContext context: Context,
) : TokenStorage {

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val mutableTokenFlow = MutableStateFlow(sharedPreferences.getString(KEY_AUTH_TOKEN, null))

    override val tokenFlow: StateFlow<String?> = mutableTokenFlow.asStateFlow()

    override suspend fun saveToken(token: String) {
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
        mutableTokenFlow.value = token
    }

    override suspend fun clearToken() {
        sharedPreferences.edit().remove(KEY_AUTH_TOKEN).apply()
        mutableTokenFlow.value = null
    }

    override fun currentToken(): String? = mutableTokenFlow.value

    private companion object {
        const val PREFS_NAME = "AppPreferences"
        const val KEY_AUTH_TOKEN = "auth_token"
    }
}
