package com.gokcank.triviaquiz

import android.app.Application
import com.google.android.gms.games.PlayGamesSdk

class TriviaQuizApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Otomatik giriş istemini SDK, uygulama ilk öne geldiğinde kendisi gösterir.
        // Kimlik yoksa (klonlanmış depo) hiç başlatılmaz — özellik kapalı kalır.
        if (BuildConfig.PGS_ENABLED) PlayGamesSdk.initialize(this)
    }
}
