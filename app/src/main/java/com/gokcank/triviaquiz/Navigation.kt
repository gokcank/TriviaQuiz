package com.gokcank.triviaquiz

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.gokcank.triviaquiz.ui.about.AboutScreen
import com.gokcank.triviaquiz.ui.favorites.FavoritesScreen
import com.gokcank.triviaquiz.ui.main.MainScreen
import com.gokcank.triviaquiz.ui.quiz.QuizScreen
import com.gokcank.triviaquiz.ui.result.ResultScreen
import com.gokcank.triviaquiz.ui.settings.SettingsScreen
import com.gokcank.triviaquiz.ui.stats.StatsScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Home)

    NavDisplay(
        backStack = backStack,
        // Yığın asla boşalmamalı — NavDisplay boş yığında çöker
        onBack    = { if (backStack.size > 1) backStack.removeLastOrNull() },
        // ViewModel'ler nav girdisine bağlanır: girdi kapanınca ViewModel temizlenir
        // (quiz'den çıkınca sayaç durur, yeni oyun bayat Finished state'i görmez)
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {

            entry<Home> {
                MainScreen(
                    onStartQuiz     = { quiz -> backStack.add(quiz) },
                    onOpenStats     = { backStack.add(Stats) },
                    onOpenFavorites = { backStack.add(Favorites) },
                    onOpenSettings  = { backStack.add(Settings) },
                    onOpenAbout     = { backStack.add(About) },
                    modifier        = Modifier.fillMaxSize()
                )
            }

            entry<Favorites> {
                FavoritesScreen(
                    onBack   = { backStack.removeLastOrNull() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            entry<Stats> {
                StatsScreen(
                    onBack   = { backStack.removeLastOrNull() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            entry<Settings> {
                SettingsScreen(
                    onBack   = { backStack.removeLastOrNull() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            entry<About> {
                AboutScreen(
                    onBack   = { backStack.removeLastOrNull() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            entry<Quiz> { key ->
                QuizScreen(
                    categoryName   = key.categoryName,
                    difficulty     = key.difficulty,
                    amount         = key.amount,
                    timed          = key.timed,
                    onQuizComplete = { score, total, bestStreak, skipped, dailyCompletedNow, gainedXp, leveledUp, newLevel, records ->
                        backStack.add(
                            Result(
                                score             = score,
                                total             = total,
                                categoryName      = key.categoryName,
                                difficulty        = key.difficulty,
                                timed             = key.timed,
                                amount            = key.amount,
                                bestStreak        = bestStreak,
                                skipped           = skipped,
                                dailyCompletedNow = dailyCompletedNow,
                                gainedXp          = gainedXp,
                                leveledUp         = leveledUp,
                                newLevel          = newLevel,
                                records           = records
                            )
                        )
                    },
                    onBack   = { backStack.removeLastOrNull() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            entry<Result> { key ->
                ResultScreen(
                    score             = key.score,
                    total             = key.total,
                    categoryName      = key.categoryName,
                    difficulty        = key.difficulty,
                    timed             = key.timed,
                    bestStreak        = key.bestStreak,
                    skipped           = key.skipped,
                    dailyCompletedNow = key.dailyCompletedNow,
                    gainedXp          = key.gainedXp,
                    leveledUp         = key.leveledUp,
                    newLevel          = key.newLevel,
                    records           = key.records,
                    onPlayAgain       = {
                        val size = backStack.size
                        repeat(size - 1) { backStack.removeLastOrNull() }
                        backStack.add(
                            Quiz(
                                categoryName = key.categoryName,
                                difficulty   = key.difficulty,
                                amount       = key.amount,
                                timed        = key.timed
                            )
                        )
                    },
                    onGoHome          = {
                        val size = backStack.size
                        repeat(size - 1) { backStack.removeLastOrNull() }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    )
}
