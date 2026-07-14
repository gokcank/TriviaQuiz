package com.gokcank.triviaquiz.util

import java.util.TimeZone

/**
 * Cihaz-yerel takvim günü (epoch'tan bu yana gün sayısı).
 * minSdk 24'te java.time yok (desugaring eklenmedi) — aritmetik hesap.
 */
fun currentEpochDay(): Long {
    val now = System.currentTimeMillis()
    return (now + TimeZone.getDefault().getOffset(now)) / 86_400_000L
}
