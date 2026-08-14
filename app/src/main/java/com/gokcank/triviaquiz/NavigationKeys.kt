package com.gokcank.triviaquiz

import androidx.navigation3.runtime.NavKey
import com.gokcank.triviaquiz.ui.quiz.AnswerRecord
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
data object Favorites : NavKey

@Serializable
data class Quiz(
    val categoryName: String = "Tüm Kategoriler",
    val difficulty: String = "easy",
    val amount: Int = 10,
    val timed: Boolean = true,           // false = süresiz mod
    val isTwoPlayer: Boolean = false     // true = 2 kişilik sırayla mod
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
    val skipped: Int = 0,
    val dailyCompletedNow: Boolean = false,
    val gainedXp: Int = 0,
    val leveledUp: Boolean = false,
    val newLevel: Int = 1,
    val records: List<AnswerRecord> = emptyList(),
    val isTwoPlayer: Boolean = false,
    val player1Score: Int = 0,
    val player2Score: Int = 0,
    val player1Total: Int = 0,
    val player2Total: Int = 0
) : NavKey
