package com.gokcank.triviaquiz.ads

import android.app.Activity
import android.content.Context
import com.gokcank.triviaquiz.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Ödüllü reklam — joker hakkı bitince "📺 reklam izle, +1 hak" akışı */
object RewardedAdManager {

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    fun preload(context: Context) {
        if (!AdsManager.canRequestAds.value || rewardedAd != null || isLoading) return
        isLoading = true
        RewardedAd.load(
            context,
            BuildConfig.ADMOB_REWARDED_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                    _isReady.value = true
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                    _isReady.value = false
                }
            }
        )
    }

    /**
     * @param onReward izleme tamamlanınca (ödül kazanılınca) çağrılır
     * @param onClosed reklam kapanınca ya da gösterilemeyince çağrılır (sayaç devamı için)
     */
    fun show(activity: Activity, onReward: () -> Unit, onClosed: () -> Unit) {
        val ad = rewardedAd ?: run {
            onClosed()
            preload(activity.applicationContext)
            return
        }
        rewardedAd = null
        _isReady.value = false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                onClosed()
                preload(activity.applicationContext)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                onClosed()
                preload(activity.applicationContext)
            }
        }
        ad.show(activity) { _ -> onReward() }
    }
}
