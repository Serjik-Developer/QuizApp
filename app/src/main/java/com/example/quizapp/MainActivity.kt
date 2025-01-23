package com.example.quizapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.Adapters.QuizAdapter
import com.example.quizapp.Interfaces.OnItemClickListener
import com.example.quizapp.Models.QuizMain
import com.example.quizapp.presentation.common.collectLatestLifecycleFlow
import com.example.quizapp.presentation.main.MainEvent
import com.example.quizapp.presentation.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnItemClickListener {
    private val quizList = mutableListOf<QuizMain>()
    private lateinit var quizAdapter: QuizAdapter
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginUser = findViewById<TextView>(R.id.login_user)
        val expUser = findViewById<TextView>(R.id.exp_user)
        val addButton = findViewById<ImageButton>(R.id.addBtn)
        val logout = findViewById<ImageButton>(R.id.log_out)
        val recyclerView = findViewById<RecyclerView>(R.id.RVEnable)

        recyclerView.layoutManager = LinearLayoutManager(this)
        quizAdapter = QuizAdapter(quizList, this)
        recyclerView.adapter = quizAdapter

        logout.setOnClickListener {
            viewModel.logout()
        }
        addButton.setOnClickListener {
            startActivity(Intent(this, AddActivity::class.java))
        }

        collectLatestLifecycleFlow(viewModel.uiState) { state ->
            loginUser.text = "Hello, ${state.userLogin}"
            expUser.text = "XP: ${state.userExp}"
            addButton.visibility = if (state.isAdmin) View.VISIBLE else View.GONE

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
            quizAdapter.notifyDataSetChanged()
        }

        collectLatestLifecycleFlow(viewModel.events) { event ->
            when (event) {
                is MainEvent.Message -> {
                    Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
                }
                MainEvent.NavigateToLogin -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadContent()
    }

    override fun onItemClick(quizId: String, quizName: String, quizDesc: String) {
        startActivity(
            Intent(this, QuizActivity::class.java)
                .putExtra("QUIZ_ID", quizId)
                .putExtra("QUIZ_NAME", quizName)
                .putExtra("QUIZ_DESC", quizDesc),
        )
    }

    override fun onEditClick(quizId: String, quizName: String, quizDesc: String) = Unit

    override fun onDeleteClick(quizId: String) = Unit

    fun addact(view: View) {
        startActivity(Intent(this, AddActivity::class.java))
    }
}
