# 📋 TriviaQuiz — Changelog

All notable changes to this project are documented here.
Bu projedeki tüm önemli değişiklikler bu dosyada belgelenmektedir.

---

## [1.2.1] — August 26, 2026 / 26 Ağustos 2026

### 🇬🇧 English

#### 🛠️ Play Console Compliance & Maintenance

* **Large-Screen Support:** Removed the portrait orientation lock; the app now rotates and resizes freely on tablets, foldables, and Chromebooks.
* **Rotation Stability:** Fixed the root cause of a relaunch crash that surfaced once the lock was removed — the screen no longer gets torn down on rotation, it now handles the configuration change in place.
* **Modernized Edge-to-Edge:** Removed deprecated window bar coloring APIs in favor of the current `WindowInsetsController` approach.
* **Dependency Update:** Upgraded a transitively-included outdated `androidx.fragment` version to 1.9.0.
* **Debug Symbols:** Added full native debug symbols to the release build.
* **About Screen:** Removed the no-longer-accurate "Closed Beta" label.
* **Ad Infrastructure:** Refreshed AdMob ad unit IDs.

### 🇹🇷 Türkçe

#### 🛠️ Play Console Uyumluluğu & Bakım

* **Büyük Ekran Desteği:** Uygulamanın dikey kilidi kaldırıldı; tablet, katlanabilir cihaz ve Chromebook'larda serbest döndürme ve yeniden boyutlandırma etkinleştirildi.
* **Döndürme Kararlılığı:** Kilit kaldırılınca ortaya çıkan bir yeniden başlatma çökmesi kök nedeninden düzeltildi — ekran artık döndürmede yeniden kurulmuyor, yapılandırma değişikliğini yerinde işliyor.
* **Ekranın Kenarlara Kadar Uzanması Modernize Edildi:** Kullanımdan kaldırılmış pencere çubuğu renklendirme API'leri kaldırıldı, güncel `WindowInsetsController` yaklaşımına geçildi.
* **Bağımlılık Güncellemesi:** Dolaylı olarak gelen eski bir `androidx.fragment` sürümü 1.9.0'a yükseltildi.
* **Hata Ayıklama Sembolleri:** Küçültülmüş (release) derlemeye tam yerel hata ayıklama sembolleri eklendi.
* **Hakkında Ekranı:** Artık geçerli olmayan "Kapalı Beta" etiketi kaldırıldı.
* **Reklam Altyapısı:** AdMob reklam kimlikleri güncellendi.

---

## [1.2.0] — August 15, 2026 / 15 Ağustos 2026

### 🇬🇧 English

#### 🌟 Major New Features

* **👥 Pass & Play (2-Player Mode):**
  * Play head-to-head with a friend by passing a single device back and forth.
  * Questions alternate between Player 1 and Player 2, with a *"📱 Your Turn!"* handoff dialog between rounds.
  * A dual result card at the end shows the winner and each player's head-to-head accuracy.
* **🎖️ Level & XP System:**
  * A leveling system from 1 to 100.
  * Earn XP from correct answers, perfect scores, and daily missions.
  * *🥉 Rookie, 🥈 Enthusiast, 🥇 Knowledge Hunter, 💎 Sage, 👑 Trivia Legend* titles and badges.
* **📋 Answer Review Mode:**
  * Review every question, the correct answer, and full details after a quiz, filtered by All / Incorrect / Correct.
* **⭐ Question Favorites & Library:**
  * Star questions with a single tap during a quiz.
  * A saved-questions screen with category filtering and a hide/reveal-answer study mode.
* **🚩 Report a Question:**
  * Flag a suspicious or incorrect question straight to the developer by email, from the quiz, review, or favorites screens.
* **🎵 Sound & Ambience:**
  * An up-tempo tick countdown sound for the final 5 seconds in timed mode.
  * A joker-use chime, special streak sounds at 3x/5x/10x correct answers, and a victory melody on the result screen.
* **📚 Expanded Question Pool:**
  * Added 197 new questions, bringing the total question bank to **433 questions** across 13 categories.

#### 🛠️ Technical Improvements & Performance

* **Android 15 / SDK 36 Compatibility:** Seamless edge-to-edge experience via `LocalActivity.current` integration and `statusBarsPadding`.
* **R8 / ProGuard Minification:** Optimized the release APK size down to **4.2 MB**.
* **Memory & Resource Improvements:** Fixed a `PlayGamesManager` memory leak risk and upgraded SoundPool to 4 channels.
* **CI/CD Integration:** Set up an automated build, lint, and test pipeline with GitHub Actions (`android-ci.yml`).

### 🇹🇷 Türkçe

#### 🌟 Öne Çıkan Yeni Özellikler

* **👥 Sırayla 2 Kişilik Oyun Modu (Pass & Play):**
  * Tek bir cihaz üzerinden arkadaşınızla sırayla yarışabilme imkanı.
  * Sorular sırayla 1. ve 2. oyuncuya yönlendirilir; tur aralarında *"📱 Sıra Sende!"* ara geçiş diyaloğu yer alır.
  * Quiz bitiminde kazananı ve kafa kafaya başarı yüzdelerini gösteren çiftli sonuç kartı.
* **🎖️ Seviye & XP Sistemi:**
  * 1'den 100'e kadar seviye ilerleme mekaniği.
  * Doğru cevaplar, tam puanlar ve günlük görevlerden XP kazanımı.
  * *🥉 Çaylak, 🥈 Meraklı, 🥇 Bilgi Avcısı, 💎 Bilge, 👑 Trivia Efsanesi* unvanları ve rozetleri.
* **📋 Cevapları İnceleme Modu:**
  * Quiz bitiminde işaretlenen tüm şıkları, doğru cevapları ve detayları filtreli (Tümü / Yanlışlar / Doğrular) olarak inceleyebilme.
* **⭐ Soru Favorileme & Kütüphane:**
  * Quiz esnasında soruları tek dokunuşla yıldızlayabilme.
  * Kaydedilen sorular ekranında kategorilere göre filtreleme ve cevap gizle/göster ile çalışma modu.
* **🚩 Hatalı Soru Bildirimi:**
  * Quiz, inceleme ve favoriler ekranlarında şüpheli veya hatalı görülen soruları tek dokunuşla geliştiriciye e-posta ile bildirebilme.
* **🎵 Ses & Ambiyans Efektleri:**
  * Zamanlı modda son 5 saniye tempolu geri sayım tik-tak efekti.
  * Joker kullanım tınısı, 3x/5x/10x doğru serilerinde özel seri sesi ve sonuç ekranı zafer melodisi.
* **📚 Soru Havuzu Genişletmesi:**
  * 197 yeni soru eklenerek toplam soru havuzu 13 kategoride **433 soruya** çıkarıldı.

#### 🛠️ Teknik İyileştirmeler & Performans

* **Android 15 / SDK 36 Uyumluluğu:** `LocalActivity.current` entegrasyonu ve `statusBarsPadding` ile kusursuz edge-to-edge deneyimi.
* **R8 / ProGuard Minification:** Release APK boyutu optimize edilerek **4.2 MB**'a düşürüldü.
* **Bellek & Kaynak İyileştirmeleri:** `PlayGamesManager` bellek sızıntısı riski giderildi, SoundPool 4 kanala yükseltildi.
* **CI/CD Entegrasyonu:** GitHub Actions ile otomatik derleme, lint ve test pipeline'ı (`android-ci.yml`) kuruldu.

---

## [1.1.1] — August 10, 2026 / 10 Ağustos 2026

### 🇬🇧 English
* First closed beta release.
* 13 categories, 236 questions.
* AdMob banner, interstitial, and rewarded ad integration.
* Play Games Services v2 leaderboard and achievements infrastructure.

### 🇹🇷 Türkçe
* İlk kapalı beta sürümü.
* 13 kategori, 236 soru.
* AdMob banner, interstitial ve rewarded reklam entegrasyonu.
* Play Games Services v2 liderlik tablosu ve başarımlar altyapısı.
