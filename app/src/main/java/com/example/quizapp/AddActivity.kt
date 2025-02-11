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
import com.example.quizapp.Adapters.QuizAdapterAdmin
import com.example.quizapp.Interfaces.OnItemClickListener
import com.example.quizapp.Models.QuizMain
import com.example.quizapp.presentation.admin.AdminEvent
import com.example.quizapp.presentation.admin.AdminQuizzesViewModel
import com.example.quizapp.presentation.common.collectLatestLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddActivity : AppCompatActivity(), OnItemClickListener {
    private val quizList = mutableListOf<QuizMain>()
    private lateinit var adapter: QuizAdapterAdmin
    private val viewModel: AdminQuizzesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        val addQuizButton = findViewById<Button>(R.id.btn_add_quiz)
        val recyclerView = findViewById<RecyclerView>(R.id.rv_admin)
        adapter = QuizAdapterAdmin(quizList, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        addQuizButton.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_edit_quiz, null)
            val editName = dialogView.findViewById<EditText>(R.id.edit_quiz_name)
            val editDesc = dialogView.findViewById<EditText>(R.id.edit_quiz_desc)

            AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Save") { _, _ ->
                    viewModel.createQuiz(
                        text = editName.text.toString(),
                        description = editDesc.text.toString(),
                    )
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        collectLatestLifecycleFlow(viewModel.uiState) { state ->
            quizList.clear()
            quizList.addAll(
                state.quizzes.map { quiz ->
                    QuizMain(
                        id = quiz.id,
                        name = quiz.name,
                        description = quiz.description,
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
        viewModel.loadQuizzes()
    }

    override fun onItemClick(quizId: String, quizName: String, quizDesc: String) {
        startActivity(
            Intent(this, AddActivityQuestions::class.java)
                .putExtra("QUIZ_ID", quizId),
        )
    }

    override fun onEditClick(quizId: String, quizName: String, quizDesc: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_quiz, null)
        val editName = dialogView.findViewById<EditText>(R.id.edit_quiz_name)
        val editDesc = dialogView.findViewById<EditText>(R.id.edit_quiz_desc)
        editName.setText(quizName)
        editDesc.setText(quizDesc)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                viewModel.updateQuiz(
                    quizId = quizId,
                    text = editName.text.toString(),
                    description = editDesc.text.toString(),
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDeleteClick(quizId: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Quiz")
            .setMessage("Are you sure you want to delete this quiz?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.deleteQuiz(quizId)
            }
            .setNegativeButton("No", null)
            .show()
    }
}
