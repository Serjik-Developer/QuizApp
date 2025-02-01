package com.example.quizapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.quizapp.presentation.common.collectLatestLifecycleFlow
import com.example.quizapp.presentation.quiz.QuizPlayerEvent
import com.example.quizapp.presentation.quiz.QuizPlayerUiState
import com.example.quizapp.presentation.quiz.QuizPlayerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainQuizActivity : AppCompatActivity() {
    private val viewModel: QuizPlayerViewModel by viewModels()
    private var renderedQuestionKey: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_quiz)

        val nextButton = findViewById<Button>(R.id.next_ans)
        val checkButton = findViewById<Button>(R.id.check_ans)
        val inputString = findViewById<EditText>(R.id.InputString)
        val inputInt = findViewById<EditText>(R.id.InputInt)
        val radioGroup = findViewById<RadioGroup>(R.id.RadioGroup)

        val questionIds = intent.getStringArrayListExtra("ALL_QUESTIONS_ID").orEmpty()

        checkButton.setOnClickListener {
            viewModel.submitAnswer(
                when (viewModel.uiState.value.questionType) {
                    "InputString" -> inputString.text.toString()
                    "InputInt" -> inputInt.text.toString()
                    "RadioButton" -> {
                        val checkedId = radioGroup.checkedRadioButtonId
                        if (checkedId == View.NO_ID) "" else findViewById<RadioButton>(checkedId).text.toString()
                    }
                    else -> ""
                },
            )
        }

        nextButton.setOnClickListener {
            viewModel.nextQuestion()
        }

        collectLatestLifecycleFlow(viewModel.uiState) { state ->
            renderState(state)
        }

        collectLatestLifecycleFlow(viewModel.events) { event ->
            when (event) {
                is QuizPlayerEvent.Message -> {
                    Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
                }
                is QuizPlayerEvent.Finished -> {
                    startActivity(
                        Intent(this, EndQuizActivity::class.java)
                            .putExtra("EXP", event.exp),
                    )
                    finish()
                }
                QuizPlayerEvent.NavigateToLogin -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }

        viewModel.startSession(questionIds)
    }

    private fun renderState(state: QuizPlayerUiState) {
        val questionView = findViewById<TextView>(R.id.Question)
        val inputString = findViewById<EditText>(R.id.InputString)
        val inputInt = findViewById<EditText>(R.id.InputInt)
        val radioGroup = findViewById<RadioGroup>(R.id.RadioGroup)
        val statusView = findViewById<TextView>(R.id.status_ans)
        val explanationView = findViewById<TextView>(R.id.explanation)
        val nextButton = findViewById<Button>(R.id.next_ans)
        val checkButton = findViewById<Button>(R.id.check_ans)

        val questionKey = buildString {
            append(state.currentQuestionIndex)
            append('|')
            append(state.questionText)
            append('|')
            append(state.answers.joinToString { it.aid })
        }
        val isNewQuestion = questionKey != renderedQuestionKey
        if (isNewQuestion) {
            renderedQuestionKey = questionKey
            inputString.setText("")
            inputInt.setText("")
            inputString.setBackgroundResource(R.drawable.input_background)
            inputInt.setBackgroundResource(R.drawable.input_background)
            radioGroup.removeAllViews()
            state.answers.forEach { answer ->
                val radioButton = RadioButton(this).apply {
                    text = answer.text
                    id = View.generateViewId()
                }
                radioGroup.addView(radioButton)
            }
        }

        questionView.text = state.questionText.ifBlank { "Loading question..." }
        statusView.visibility = if (state.resultMessage.isBlank()) View.GONE else View.VISIBLE
        statusView.text = state.resultMessage
        explanationView.visibility = if (state.explanation.isBlank()) View.GONE else View.VISIBLE
        explanationView.text = state.explanation

        inputString.visibility = if (state.questionType == "InputString") View.VISIBLE else View.GONE
        inputInt.visibility = if (state.questionType == "InputInt") View.VISIBLE else View.GONE
        radioGroup.visibility = if (state.questionType == "RadioButton") View.VISIBLE else View.GONE

        inputString.isEnabled = !state.hasSubmitted && !state.isLoading
        inputInt.isEnabled = !state.hasSubmitted && !state.isLoading
        for (index in 0 until radioGroup.childCount) {
            radioGroup.getChildAt(index).isEnabled = !state.hasSubmitted && !state.isLoading
        }

        if (state.hasSubmitted) {
            val isCorrect = state.resultMessage.startsWith("Correct")
            val feedbackColor = if (isCorrect) Color.GREEN else Color.RED
            when (state.questionType) {
                "InputString" -> inputString.setBackgroundColor(feedbackColor)
                "InputInt" -> inputInt.setBackgroundColor(feedbackColor)
            }
        }

        nextButton.text = if (state.currentQuestionIndex >= state.totalQuestions) "Finish Quiz" else "Next Question"
        nextButton.isEnabled = !state.isLoading
        checkButton.isEnabled = !state.isLoading
    }
}
