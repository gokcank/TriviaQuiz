package com.gokcank.triviaquiz.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * UMP onay akışı + Mobile Ads SDK başlatma.
 * Onay alınıp SDK başlayana kadar hiçbir reklam isteği yapılmaz.
 */
object AdsManager {

    private val _canRequestAds = MutableStateFlow(false)
    val canRequestAds: StateFlow<Boolean> = _canRequestAds.asStateFlow()

    private val initialized = AtomicBoolean(false)

    /** MainActivity.onCreate'te çağrılır */
    fun gatherConsentAndInitialize(activity: Activity) {
        val consentInfo = UserMessagingPlatform.getConsentInformation(activity)
        val params = ConsentRequestParameters.Builder().build()

        consentInfo.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { _ ->
                    if (consentInfo.canRequestAds()) initializeMobileAds(activity)
                }
            },
            { _ ->
                // Onay bilgisi alınamadı (örn. çevrimdışı) — önceki onay geçerliyse devam
                if (consentInfo.canRequestAds()) initializeMobileAds(activity)
            }
        )

        // Önceki oturumdan onay zaten varsa formu beklemeden başlat
        if (consentInfo.canRequestAds()) initializeMobileAds(activity)
    }

    private fun initializeMobileAds(context: Context) {
        if (initialized.getAndSet(true)) return
        val appContext = context.applicationContext
        MobileAds.initialize(appContext) {
            _canRequestAds.value = true
            InterstitialAdManager.preload(appContext)
            RewardedAdManager.preload(appContext)
        }
    }
}
