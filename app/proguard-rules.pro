# kotlinx.serialization — @Serializable modeller (soru bankası, istatistikler, nav anahtarları)
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class com.gokcank.triviaquiz.** {
    *** Companion;
}
-keepclasseswithmembers class com.gokcank.triviaquiz.** {
    kotlinx.serialization.KSerializer serializer(...);
}
