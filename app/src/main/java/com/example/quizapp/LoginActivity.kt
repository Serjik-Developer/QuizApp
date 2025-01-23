package com.example.quizapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.quizapp.presentation.auth.LoginEvent
import com.example.quizapp.presentation.auth.LoginViewModel
import com.example.quizapp.presentation.common.collectLatestLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val login = findViewById<EditText>(R.id.log_login)
        val password = findViewById<EditText>(R.id.log_pass)
        val logMe = findViewById<Button>(R.id.LogMe)

        logMe.setOnClickListener {
            viewModel.login(
                login = login.text.toString(),
                password = password.text.toString(),
            )
        }

        collectLatestLifecycleFlow(viewModel.uiState) { state ->
            logMe.isEnabled = !state.isLoading
            login.isEnabled = !state.isLoading
            password.isEnabled = !state.isLoading
        }

        collectLatestLifecycleFlow(viewModel.events) { event ->
            when (event) {
                is LoginEvent.Error -> {
                    Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
                }
                LoginEvent.NavigateToHome -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
    }

    fun needReg(view: View) {
        startActivity(Intent(this, RegActivity::class.java))
    }
}
