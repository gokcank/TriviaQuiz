plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
}

// Gizli değerler local.properties'ten okunur (repo dışı); dosya/anahtar yoksa
// test/varsayılan değerler kullanılır ki depoyu klonlayan herkes derleyebilsin.
val localPropsMap: Map<String, String> = rootProject.file("local.properties")
    .takeIf { it.exists() }
    ?.readLines()
    ?.filter { line -> line.isNotBlank() && !line.startsWith('#') && '=' in line }
    ?.associate { line ->
        val idx = line.indexOf('=')
        line.substring(0, idx).trim() to line.substring(idx + 1).trim()
    }
    ?: emptyMap()

fun secret(key: String, default: String): String =
    localPropsMap[key]?.takeIf { it.isNotBlank() } ?: default

android {
    namespace = "com.gokcank.triviaquiz"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.gokcank.triviaquiz"
        minSdk = 24
        targetSdk = 36
        versionCode = 4
        versionName = "1.2.0"

        // AdMob — gerçek ID'ler local.properties'te; yoksa Google'ın resmî test ID'leri
        manifestPlaceholders["admobAppId"] =
            secret("ADMOB_APP_ID", "ca-app-pub-3940256099942544~3347511713")
        buildConfigField("String", "ADMOB_BANNER_ID",
            "\"${secret("ADMOB_BANNER_ID", "ca-app-pub-3940256099942544/6300978111")}\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL_ID",
            "\"${secret("ADMOB_INTERSTITIAL_ID", "ca-app-pub-3940256099942544/1033173712")}\"")
        buildConfigField("String", "ADMOB_REWARDED_ID",
            "\"${secret("ADMOB_REWARDED_ID", "ca-app-pub-3940256099942544/5224354917")}\"")

        // Play Games Services — kimlikler local.properties'te; yoksa özellik kendini kapatır.
        // Sayısal APP_ID meta-data'ya doğrudan yazılamaz (aapt int'e çevirir, SDK string
        // bekler ve çöker) → @string kaynağı üzerinden verilir.
        val pgsAppId = secret("PGS_APP_ID", "0")
        resValue("string", "game_services_project_id", pgsAppId)
        buildConfigField("boolean", "PGS_ENABLED",
            (pgsAppId != "0" && pgsAppId.all { it.isDigit() }).toString())
        listOf(
            "PGS_LB_TOTAL_CORRECT", "PGS_LB_BEST_STREAK",
            "PGS_ACH_FIRST_GAME", "PGS_ACH_GAMES_10", "PGS_ACH_GAMES_50",
            "PGS_ACH_CORRECT_100", "PGS_ACH_CORRECT_500", "PGS_ACH_PERFECT",
            "PGS_ACH_STREAK_10", "PGS_ACH_HARD_MASTER",
            "PGS_ACH_DAILY_FIRST", "PGS_ACH_DAILY_7"
        ).forEach { key ->
            buildConfigField("String", key, "\"${secret(key, "")}\"")
        }
    }

    // Release imzası: dört anahtar da local.properties'te varsa gerçek keystore,
    // yoksa debug imza (derleme asla kırılmaz)
    val releaseSigningReady = listOf(
        "RELEASE_STORE_FILE", "RELEASE_STORE_PASSWORD", "RELEASE_KEY_ALIAS", "RELEASE_KEY_PASSWORD"
    ).all { !localPropsMap[it].isNullOrBlank() }

    signingConfigs {
        if (releaseSigningReady) {
            create("release") {
                storeFile     = rootProject.file(localPropsMap.getValue("RELEASE_STORE_FILE"))
                storePassword = localPropsMap["RELEASE_STORE_PASSWORD"]
                keyAlias      = localPropsMap["RELEASE_KEY_ALIAS"]
                keyPassword   = localPropsMap["RELEASE_KEY_PASSWORD"]
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = if (releaseSigningReady) signingConfigs.getByName("release")
                            else signingConfigs.getByName("debug")
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      aidl = false
      buildConfig = true
      shaders = false
      resValues = true   // PGS APP_ID @string kaynağı için
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests: jUnit, coroutines, Android runner
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)

  // Instrumented tests: jUnit rules and runners
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)

  // Navigation
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)

  // JSON — yerel soru bankası (assets/trivia_tr.json)
  implementation(libs.kotlinx.serialization.json)

  // Ayarlar kalıcılığı
  implementation(libs.androidx.datastore.preferences)

  // Reklamlar — Google Mobile Ads (UMP dahil)
  implementation(libs.play.services.ads)

  // Play Games Services v2 — başarımlar + liderlik tabloları
  implementation(libs.play.services.games.v2)
  implementation(libs.kotlinx.coroutines.play.services)

  // Play Services'in dolaylı getirdiği eski androidx.fragment sürümünü güncellemek için
  implementation(libs.androidx.fragment.ktx)

  // Google Fonts
  implementation(libs.androidx.compose.ui.google.fonts)

  // Material Icons
  implementation(libs.androidx.compose.material.icons.core)
}
