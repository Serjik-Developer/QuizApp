package com.example.quizapp.domain.model

data class QuestionDetail(
    val text: String,
    val type: String,
    val answers: List<AnswerOption>,
)
