package com.example.quizapp.data.remote

class ApiException(
    message: String,
    val code: Int? = null,
) : Exception(message)
