package com.gokcank.triviaquiz

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object Home : NavKey

@Serializable
data object Settings : NavKey

@Serializable
data object About : NavKey

@Serializable
data object Stats : NavKey

@Serializable
data class Quiz(
    val categoryName: String = "Tüm Kategoriler",
    val difficulty: String = "easy",
    val amount: Int = 10,
    val timed: Boolean = true           // false = süresiz mod
) : NavKey

@Serializable
data class Result(
    val score: Int,
    val total: Int,
    val categoryName: String,
    val difficulty: String,
    val timed: Boolean,
    val amount: Int = 10,
    val bestStreak: Int = 0,
    val skipped: Int = 0
) : NavKey
