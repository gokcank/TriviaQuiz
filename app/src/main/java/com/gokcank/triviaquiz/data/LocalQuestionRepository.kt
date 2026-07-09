package com.gokcank.triviaquiz.data

import android.content.Context
import com.gokcank.triviaquiz.data.model.LocalQuestion
import kotlinx.serialization.json.Json

class LocalQuestionRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    /** Tüm soruları assets'ten yükler (lazy + önbellek) */
    private val allQuestions: List<LocalQuestion> by lazy {
        context.assets.open("trivia_tr.json")
            .bufferedReader()
            .use { it.readText() }
            .let { json.decodeFromString<List<LocalQuestion>>(it) }
            .shuffled()
    }

    /**
     * @param amount     Kaç soru döndürülsün
     * @param category   null = tüm kategoriler
     * @param difficulty null = tüm zorluklar
     */
    fun getQuestions(
        amount: Int,
        category: String? = null,
        difficulty: String? = null
    ): List<LocalQuestion> {
        val filtered = allQuestions.filter { q ->
            (category == null || q.category == category) &&
            (difficulty == null || q.difficulty == difficulty)
        }.shuffled()

        if (filtered.size < amount) return filtered   // istenen sayı yoksa hepsini dön
        return filtered.take(amount)
    }
}
