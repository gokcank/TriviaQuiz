package com.gokcank.triviaquiz.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalQuestion(
    val category: String,
    val difficulty: String,
    val question: String,
    @SerialName("correct_answer")    val correctAnswer: String,
    @SerialName("incorrect_answers") val incorrectAnswers: List<String>
)
