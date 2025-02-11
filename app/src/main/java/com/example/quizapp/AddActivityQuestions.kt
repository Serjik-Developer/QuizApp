package com.example.quizapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.Adapters.QuestionAdapterAdmin
import com.example.quizapp.Interfaces.OnItemClickListenerQuestions
import com.example.quizapp.Models.Questions
import com.example.quizapp.presentation.admin.AdminEvent
import com.example.quizapp.presentation.admin.AdminQuestionsViewModel
import com.example.quizapp.presentation.common.collectLatestLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddActivityQuestions : AppCompatActivity(), OnItemClickListenerQuestions {
    private val questionList = mutableListOf<Questions>()
    private lateinit var adapter: QuestionAdapterAdmin
    private lateinit var quizId: String
    private val viewModel: AdminQuestionsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_questions)

        quizId = intent.getStringExtra("QUIZ_ID").orEmpty()
        val addQuestionButton = findViewById<Button>(R.id.btn_add_question)
        val recyclerView = findViewById<RecyclerView>(R.id.rv_admin_questions)

        adapter = QuestionAdapterAdmin(questionList, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        addQuestionButton.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_question, null)
            val editName = dialogView.findViewById<EditText>(R.id.edit_question_name_add)
            val radioGroup = dialogView.findViewById<RadioGroup>(R.id.RadioGroupAdd)

            AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Save") { _, _ ->
                    val checkedId = radioGroup.checkedRadioButtonId
                    if (checkedId == RadioGroup.NO_ID) {
                        Toast.makeText(this, "Choose a question type", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    val type = dialogView.findViewById<RadioButton>(checkedId).text.toString()
                    viewModel.createQuestion(
                        question = editName.text.toString(),
                        type = type,
                    )
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        collectLatestLifecycleFlow(viewModel.uiState) { state ->
            questionList.clear()
            questionList.addAll(
                state.questions.map { question ->
                    Questions(
                        id = question.id,
                        qid = question.qid,
                        question = question.question,
                        type = question.type,
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
        viewModel.loadQuestions(quizId)
    }

    override fun onItemClick(qid: String, question: String) {
        startActivity(
            Intent(this, AddActivityAnswers::class.java)
                .putExtra("QUESTION_ID", qid),
        )
    }

    override fun onEditClick(qid: String, question: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_question, null)
        val editName = dialogView.findViewById<EditText>(R.id.edit_question_name)
        editName.setText(question)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                viewModel.updateQuestion(
                    questionId = qid,
                    question = editName.text.toString(),
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDeleteClick(qid: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete question")
            .setMessage("Are you sure you want to delete this question?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.deleteQuestion(qid)
            }
            .setNegativeButton("No", null)
            .show()
    }
}
