package com.example.quizapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.quizapp.presentation.auth.RegisterEvent
import com.example.quizapp.presentation.auth.RegisterViewModel
import com.example.quizapp.presentation.common.collectLatestLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegActivity : AppCompatActivity() {
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg)

        val login = findViewById<EditText>(R.id.reg_login)
        val password = findViewById<EditText>(R.id.reg_pasword)
        val regMe = findViewById<Button>(R.id.RegMe)

        regMe.setOnClickListener {
            viewModel.register(
                login = login.text.toString(),
                password = password.text.toString(),
            )
        }

        collectLatestLifecycleFlow(viewModel.uiState) { state ->
            regMe.isEnabled = !state.isLoading
            login.isEnabled = !state.isLoading
            password.isEnabled = !state.isLoading
        }

        collectLatestLifecycleFlow(viewModel.events) { event ->
            when (event) {
                is RegisterEvent.Error -> {
                    Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
                }
                RegisterEvent.NavigateToHome -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
    }

    fun needLog(view: View) {
        startActivity(Intent(this, LoginActivity::class.java))
    }
}
