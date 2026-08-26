# 📋 TriviaQuiz — Değişiklik Günlüğü (Changelog)

Tüm önemli sürüm değişiklikleri bu dosyada belgelenmektedir.

---

## [1.2.1] — 26 Ağustos 2026

### 🛠️ Play Console Uyumluluğu & Bakım

* **Büyük Ekran Desteği:** Uygulamanın dikey kilidi kaldırıldı; tablet, katlanabilir cihaz ve Chromebook'larda serbest döndürme ve yeniden boyutlandırma etkinleştirildi.
* **Döndürme Kararlılığı:** Kilit kaldırılınca ortaya çıkan bir yeniden başlatma çökmesi kök nedeninden düzeltildi — ekran artık döndürmede yeniden kurulmuyor, yapılandırma değişikliğini yerinde işliyor.
* **Ekranın Kenarlara Kadar Uzanması Modernize Edildi:** Kullanımdan kaldırılmış pencere çubuğu renklendirme API'leri kaldırıldı, güncel `WindowInsetsController` yaklaşımına geçildi.
* **Bağımlılık Güncellemesi:** Dolaylı olarak gelen eski bir `androidx.fragment` sürümü 1.9.0'a yükseltildi.
* **Hata Ayıklama Sembolleri:** Küçültülmüş (release) derlemeye tam yerel hata ayıklama sembolleri eklendi.
* **Hakkında Ekranı:** Artık geçerli olmayan "Kapalı Beta" etiketi kaldırıldı.
* **Reklam Altyapısı:** AdMob reklam kimlikleri güncellendi.

---

## [1.2.0] — 15 Ağustos 2026

### 🌟 Öne Çıkan Yeni Özellikler

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

### 🛠️ Teknik İyileştirmeler & Performans

* **Android 15 / SDK 36 Uyumluluğu:** `LocalActivity.current` entegrasyonu ve `statusBarsPadding` ile kusursuz edge-to-edge deneyimi.
* **R8 / ProGuard Minification:** Release APK boyutu optimize edilerek **4.2 MB**'a düşürüldü.
* **Bellek & Kaynak İyileştirmeleri:** `PlayGamesManager` bellek sızıntısı riski giderildi, SoundPool 4 kanala yükseltildi.
* **CI/CD Entegrasyonu:** GitHub Actions ile otomatik derleme, lint ve test pipeline'ı (`android-ci.yml`) kuruldu.

---

## [1.1.1] — 10 Ağustos 2026
* İlk kapalı beta sürümü.
* 13 kategori, 236 soru.
* AdMob banner, interstitial ve rewarded reklam entegrasyonu.
* Play Games Services v2 liderlik tablosu ve başarımlar altyapısı.
