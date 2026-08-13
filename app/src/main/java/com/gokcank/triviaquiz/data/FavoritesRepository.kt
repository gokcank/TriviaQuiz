package com.gokcank.triviaquiz.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.favoritesDataStore: DataStore<Preferences> by preferencesDataStore(name = "favorites")

@Serializable
data class FavoriteQuestion(
    val question: String,
    val correctAnswer: String,
    val incorrectAnswers: List<String>,
    val category: String,
    val difficulty: String,
    val timestamp: Long = System.currentTimeMillis()
)

class FavoritesRepository(context: Context) {

    private val dataStore = context.favoritesDataStore
    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val FAVORITES_JSON = stringPreferencesKey("favorites_json")
    }

    val favorites: Flow<List<FavoriteQuestion>> = dataStore.data.map { prefs ->
        val raw = prefs[Keys.FAVORITES_JSON]
        if (raw.isNullOrBlank()) emptyList()
        else runCatching { json.decodeFromString<List<FavoriteQuestion>>(raw) }.getOrDefault(emptyList())
    }

    fun isFavorite(questionText: String): Flow<Boolean> = favorites.map { list ->
        list.any { it.question.trim().equals(questionText.trim(), ignoreCase = true) }
    }

    suspend fun toggleFavorite(question: FavoriteQuestion): Boolean {
        var isNowFav = false
        dataStore.edit { prefs ->
            val raw = prefs[Keys.FAVORITES_JSON]
            val currentList: List<FavoriteQuestion> = if (raw.isNullOrBlank()) emptyList()
            else runCatching { json.decodeFromString<List<FavoriteQuestion>>(raw) }.getOrDefault(emptyList())

            val existingIndex = currentList.indexOfFirst {
                it.question.trim().equals(question.question.trim(), ignoreCase = true)
            }

            val updatedList = if (existingIndex >= 0) {
                // Zaten favoride -> çıkar
                isNowFav = false
                currentList.toMutableList().apply { removeAt(existingIndex) }
            } else {
                // Favoriye ekle
                isNowFav = true
                currentList + question
            }

            prefs[Keys.FAVORITES_JSON] = json.encodeToString<List<FavoriteQuestion>>(updatedList)
        }
        return isNowFav
    }

    suspend fun removeFavorite(questionText: String) {
        dataStore.edit { prefs ->
            val raw = prefs[Keys.FAVORITES_JSON]
            if (!raw.isNullOrBlank()) {
                val currentList = runCatching { json.decodeFromString<List<FavoriteQuestion>>(raw) }.getOrDefault(emptyList())
                val updated = currentList.filterNot { it.question.trim().equals(questionText.trim(), ignoreCase = true) }
                prefs[Keys.FAVORITES_JSON] = json.encodeToString<List<FavoriteQuestion>>(updated)
            }
        }
    }
}
