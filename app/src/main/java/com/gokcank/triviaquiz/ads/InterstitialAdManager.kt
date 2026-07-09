package com.gokcank.triviaquiz.ads

import android.app.Activity
import android.content.Context
import com.gokcank.triviaquiz.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Tam ekran geçiş reklamı — her [GAMES_PER_AD] oyunda bir, sonuç ekranında gösterilir.
 * Sayaç process ömrüyle sınırlıdır (uygulama yeniden başlarsa sıfırlanır).
 */
object InterstitialAdManager {

    private const val GAMES_PER_AD = 3

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var gamesSinceLastAd = 0

    fun preload(context: Context) {
        if (!AdsManager.canRequestAds.value || interstitialAd != null || isLoading) return
        isLoading = true
        InterstitialAd.load(
            context,
            BuildConfig.ADMOB_INTERSTITIAL_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }

    /** Her oyun bittiğinde çağrılır; sırası geldiyse reklamı gösterir */
    fun onGameFinished(activity: Activity) {
        gamesSinceLastAd++
        if (gamesSinceLastAd < GAMES_PER_AD) return

        val ad = interstitialAd ?: run {
            preload(activity.applicationContext)
            return
        }
        gamesSinceLastAd = 0
        interstitialAd = null

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preload(activity.applicationContext)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                preload(activity.applicationContext)
            }
        }
        ad.show(activity)
    }
}
