package com.gokcank.triviaquiz.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.triviaquiz.data.DailyRepository
import com.gokcank.triviaquiz.data.DailyState
import com.gokcank.triviaquiz.data.PlayerLevel
import com.gokcank.triviaquiz.data.XpRepository
import com.gokcank.triviaquiz.data.calculateLevel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DailyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DailyRepository(application)
    private val xpRepository = XpRepository(application)

    /** null = bugünün görevi henüz üretilmedi (ilk refresh'e dek) */
    val daily: StateFlow<DailyState?> = repository.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val playerLevel: StateFlow<PlayerLevel> = xpRepository.playerLevel
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), calculateLevel(0))

    /** Gün devrini yakalar; gerekiyorsa bugünün görevini üretir */
    fun refresh() {
        viewModelScope.launch { repository.ensureToday() }
    }
}
