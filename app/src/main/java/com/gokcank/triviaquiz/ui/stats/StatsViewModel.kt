package com.gokcank.triviaquiz.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.triviaquiz.data.GameStats
import com.gokcank.triviaquiz.data.PlayerLevel
import com.gokcank.triviaquiz.data.StatsRepository
import com.gokcank.triviaquiz.data.XpRepository
import com.gokcank.triviaquiz.data.calculateLevel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StatsRepository(application)
    private val xpRepository = XpRepository(application)

    val stats: StateFlow<GameStats> = repository.stats
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = GameStats()
        )

    val playerLevel: StateFlow<PlayerLevel> = xpRepository.playerLevel
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = calculateLevel(0)
        )
}
