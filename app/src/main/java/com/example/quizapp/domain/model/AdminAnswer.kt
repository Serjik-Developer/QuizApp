package com.example.quizapp.domain.model

data class AdminAnswer(
    val qid: String,
    val aid: String,
    val text: String,
    val explanation: String,
    val trueValue: String,
)
