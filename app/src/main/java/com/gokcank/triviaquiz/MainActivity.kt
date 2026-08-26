package com.gokcank.triviaquiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gokcank.triviaquiz.ads.AdsManager
import com.gokcank.triviaquiz.data.SettingsRepository
import com.gokcank.triviaquiz.data.ThemeMode
import com.gokcank.triviaquiz.games.PlayGamesManager
import com.gokcank.triviaquiz.theme.TriviaQuizTheme

class MainActivity : ComponentActivity() {

  private val settingsRepository by lazy { SettingsRepository(applicationContext) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    AdsManager.gatherConsentAndInitialize(this)
    PlayGamesManager.init(this)

    enableEdgeToEdge()
    setContent {
      val themeMode by settingsRepository.themeMode.collectAsStateWithLifecycle(initialValue = null)
      // İlk değer gelene dek pencere arkaplanı görünür (themes.xml), yanlış tema çizilmez
      val mode = themeMode ?: return@setContent

      val isDark = when (mode) {
        ThemeMode.LIGHT  -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        else             -> true
      }

      LaunchedEffect(isDark) {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = !isDark
        insetsController.isAppearanceLightNavigationBars = !isDark
      }

      TriviaQuizTheme(darkTheme = isDark) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { MainNavigation() }
      }
    }
  }
}
