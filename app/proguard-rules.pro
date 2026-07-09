# ── kotlinx.serialization ────────────────────────────────────────────────────
# @Serializable sınıfları ve üretilen $$serializer companion'ları koru
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod

-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers @kotlinx.serialization.Serializable class * { *; }

-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class **$$serializer {
    *;
}

# Kotlinx serialization runtime
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.** { *; }

# ── Navigation NavKey sınıfları ──────────────────────────────────────────────
# data object / data class olan NavKey'ler refleksiyonla erişiliyor
-keep class com.gokcank.triviaquiz.Home { *; }
-keep class com.gokcank.triviaquiz.Settings { *; }
-keep class com.gokcank.triviaquiz.About { *; }
-keep class com.gokcank.triviaquiz.Stats { *; }
-keep class com.gokcank.triviaquiz.Quiz { *; }
-keep class com.gokcank.triviaquiz.Result { *; }

# ── Uygulama geneli: Companion ve serializer helper'lar ─────────────────────
-keepclassmembers class com.gokcank.triviaquiz.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# ── Reklam singletonları (Kotlin object) ─────────────────────────────────────
-keep class com.gokcank.triviaquiz.ads.AdsManager { *; }
-keep class com.gokcank.triviaquiz.ads.InterstitialAdManager { *; }
-keep class com.gokcank.triviaquiz.ads.RewardedAdManager { *; }
-keep class com.gokcank.triviaquiz.ads.BannerAdKt { *; }

# ── Data model sınıfları (DataStore + JSON) ──────────────────────────────────
-keep class com.gokcank.triviaquiz.data.** { *; }
