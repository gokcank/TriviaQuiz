package com.gokcank.triviaquiz.ads

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gokcank.triviaquiz.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/** Uyarlanabilir (adaptive) banner — onay alınmadıysa hiç render edilmez */
@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    val canRequestAds by AdsManager.canRequestAds.collectAsStateWithLifecycle()
    if (!canRequestAds) return

    BoxWithConstraints(modifier) {
        val adWidthDp = maxWidth.value.toInt()
        AndroidView(
            factory = { context ->
                AdView(context).apply {
                    setAdSize(
                        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp)
                    )
                    adUnitId = BuildConfig.ADMOB_BANNER_ID
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
