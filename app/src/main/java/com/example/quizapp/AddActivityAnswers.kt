package com.example.quizapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.Adapters.AnswersAdapterAdmin
import com.example.quizapp.Interfaces.OnItemClickListenerAnswers
import com.example.quizapp.Models.AnswersAdmin
import com.example.quizapp.presentation.admin.AdminAnswersViewModel
import com.example.quizapp.presentation.admin.AdminEvent
import com.example.quizapp.presentation.common.collectLatestLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddActivityAnswers : AppCompatActivity(), OnItemClickListenerAnswers {
    private val answersList = mutableListOf<AnswersAdmin>()
    private lateinit var adapter: AnswersAdapterAdmin
    private lateinit var questionId: String
    private val viewModel: AdminAnswersViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_answers)

        questionId = intent.getStringExtra("QUESTION_ID").orEmpty()
        val addAnswerButton = findViewById<Button>(R.id.btn_add_answers)
        val recyclerView = findViewById<RecyclerView>(R.id.rv_answers)

        adapter = AnswersAdapterAdmin(answersList, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        addAnswerButton.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_edit_answers, null)
            val editName = dialogView.findViewById<EditText>(R.id.edit_answer_name)
            val editExplanation = dialogView.findViewById<EditText>(R.id.edit_answer_explanation)
            val editTrue = dialogView.findViewById<EditText>(R.id.edit_answer_true)

            AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Save") { _, _ ->
                    viewModel.createAnswer(
                        text = editName.text.toString(),
                        explanation = editExplanation.text.toString(),
                        trueValue = editTrue.text.toString(),
                    )
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        collectLatestLifecycleFlow(viewModel.uiState) { state ->
            answersList.clear()
            answersList.addAll(
                state.answers.map { answer ->
                    AnswersAdmin(
                        qid = answer.qid,
                        aid = answer.aid,
                        text = answer.text,
                        explanation = answer.explanation,
                        trueQ = answer.trueValue,
                    )
                },
            )
            adapter.notifyDataSetChanged()
        }

        collectLatestLifecycleFlow(viewModel.events) { event ->
            when (event) {
                is AdminEvent.Message -> {
                    Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
                }
                AdminEvent.NavigateToLogin -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadAnswers(questionId)
    }

    override fun onEditClick(aid: String, text: String, explanation: String, trueQustion: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_answers, null)
        val editName = dialogView.findViewById<EditText>(R.id.edit_answer_name)
        val editExplanation = dialogView.findViewById<EditText>(R.id.edit_answer_explanation)
        val editTrue = dialogView.findViewById<EditText>(R.id.edit_answer_true)

        editName.setText(text)
        editExplanation.setText(explanation)
        editTrue.setText(trueQustion)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                viewModel.updateAnswer(
                    answerId = aid,
                    text = editName.text.toString(),
                    explanation = editExplanation.text.toString(),
                    trueValue = editTrue.text.toString(),
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDeleteClick(aid: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete answer")
            .setMessage("Are you sure you want to delete this answer?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.deleteAnswer(aid)
            }
            .setNegativeButton("No", null)
            .show()
    }
}
