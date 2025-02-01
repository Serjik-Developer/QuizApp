package com.example.quizapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.quizapp.presentation.common.collectLatestLifecycleFlow
import com.example.quizapp.presentation.quiz.QuizOverviewEvent
import com.example.quizapp.presentation.quiz.QuizOverviewViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuizActivity : AppCompatActivity() {
    private val viewModel: QuizOverviewViewModel by viewModels()
    private lateinit var quizId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        quizId = intent.getStringExtra("QUIZ_ID").orEmpty()
        val quizName = intent.getStringExtra("QUIZ_NAME").orEmpty()
        val quizDesc = intent.getStringExtra("QUIZ_DESC").orEmpty()

        val startQuizButton = findViewById<Button>(R.id.start_quiz)
        val nameQuiz = findViewById<TextView>(R.id.quiz_name)
        val descriptionView = findViewById<TextView>(R.id.QuestionNumber)
        val countAnswers = findViewById<TextView>(R.id.count_ans)

        nameQuiz.text = quizName
        descriptionView.text = quizDesc
        startQuizButton.isEnabled = false

        startQuizButton.setOnClickListener {
            startActivity(
                Intent(this, MainQuizActivity::class.java)
                    .putExtra("QUIZ_ID", quizId)
                    .putStringArrayListExtra(
                        "ALL_QUESTIONS_ID",
                        ArrayList(viewModel.uiState.value.questionIds),
                    ),
            )
        }

        collectLatestLifecycleFlow(viewModel.uiState) { state ->
            countAnswers.text = state.questionCountLabel
            startQuizButton.isEnabled = !state.isLoading && state.questionIds.isNotEmpty()
        }

        collectLatestLifecycleFlow(viewModel.events) { event ->
            when (event) {
                is QuizOverviewEvent.Message -> {
                    Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
                }
                QuizOverviewEvent.NavigateToLogin -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }

        viewModel.loadQuiz(quizId)
    }
}
