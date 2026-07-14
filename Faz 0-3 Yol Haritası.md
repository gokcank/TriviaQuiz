# TriviaQuiz — Sürüm 2.0 Yol Haritası

4 fazlı yol haritası (scratch_notes.md'deki fikirlerden türetildi). Fazlar tek tek onaylanarak ilerletildi — faz atlama yok.

---

## Faz 0 — Temizlik ve Paket Adı (TAMAMLANDI, 2026-07-09)

- Paket adı `com.example.triviaquiz` → **`com.gokcank.triviaquiz`**
- Ölü OpenTDB/Retrofit kodu ve bozuk şablon testleri silindi
- Retrofit/OkHttp bağımlılıkları kaldırıldı
- `buildConfig = true`
- INTERNET izni kaldırıldı (fontlar GMS provider'la çalışıyor, cihazda doğrulandı — Faz 3'te AdMob için geri eklendi)
- Görünen ad "TriviaQuiz" olarak kaldı

## Faz 1 — Ayarlar ve Hakkında (TAMAMLANDI, 2026-07-09)

- Ayarlar: DataStore Preferences, yalnızca soru süresi (15/30/45/60 sn)
- Hakkında: sürüm = `BuildConfig.VERSION_NAME`, geliştirici "gokcank"
- Kapsam dışı bırakılanlar: tema ayarı yok (koyu tema sabit), ses/titreşim toggle'ları → Faz 2, lisans ekranı → Faz 3 (ölü link koymama prensibi: gizlilik politikası/Play linki de Faz 3'e ertelendi)
- `SectionCard` / `SelectableChip` → `ui/components/Common.kt`'de paylaşımlı hale getirildi

## Faz 2 — İstatistik, Ses/Titreşim, Jokerler (TAMAMLANDI, 2026-07-09)

- İstatistikler: `StatsRepository`, ayrı "stats" DataStore'da JSON `GameStats` (Geç'ilen sorular loglanmaz, sorular kendi kategorisiyle sayılır)
- Ses + titreşim: `FeedbackManager` (SoundPool + Vibrator); WAV'ler PowerShell script'iyle sentezlendi → `res/raw`; VIBRATE izni eklendi
- Joker sistemi: 50:50 / +15sn / Geç — haklar soru sayısına ölçekli (10 soru → 1'er hak, 15-20 soru → 2'şer hak); zamanlayıcı while-döngülü decrement'e çevrildi ki +15sn kaybolmasın
- Seri rozeti (🔥 x2+) + Paylaş (`ACTION_SEND`)
- Bilinen kozmetik kusur: Sonuç ekranı Geç'ilen soruyu "Yanlış" sayacında gösteriyor (total-score hesabından kaynaklanıyor)
- Ses işitsel olarak doğrulanmadı (uzaktan test edilemez)

## Faz 3 — İmzalama, AdMob, Play Store Hazırlığı (BÜYÜK ÖLÇÜDE TAMAMLANDI, 2026-07-10)

**İmzalama:**
- `keystore/release.jks` üretildi, `local.properties`'te 4 anahtar (`RELEASE_STORE_FILE/PASSWORD`, `RELEASE_KEY_ALIAS/PASSWORD`)
- `app/build.gradle.kts`: dört değer de varsa gerçek imza, yoksa debug imzaya düşer (`releaseSigningReady`) — klonlayan herkes derleyebilir
- `release` buildType: `isMinifyEnabled` + `isShrinkResources` aktif, `proguard-rules.pro` (kotlinx-serialization + Room/WorkManager/NavKey/reklam singleton keep kuralları)
- İmzalı release **AAB** + **APK** üretildi, `apksigner verify` ile doğrulandı (CN=Gokcan Kahraman — gerçek keystore, debug değil)
- `release.jks` + parola OneDrive kişisel kasaya yedeklendi ✓

**AdMob + reklamlar** (`app/src/main/java/com/gokcank/triviaquiz/ads/`):
- `AdsManager.kt` — UMP consent akışı + `MobileAds.initialize`; `canRequestAds` StateFlow tüm reklam bileşenlerine açık
- `BannerAd.kt` — adaptive banner, MainScreen ve QuizScreen altında
- `InterstitialAdManager.kt` — her 3 oyunda bir, Sonuç ekranında
- `RewardedAdManager.kt` — joker hakkı bitince 📺 modu, izlenince +1 hak
- Gerçek AdMob ID'leri `local.properties`'e girildi (yoksa Google test ID'lerine düşer)
- Cihazda doğrulandı: init hatasız, banner/interstitial/rewarded hepsi geliyor (gerçek reklamlar test ID'lere göre biraz geç yükleniyor — beklenen davranış: consent+init zinciri + gerçek müzayede gecikmesi, hesap yeni olduğu için başta biraz daha belirgin)

**UI/diğer:**
- QuizScreen ve MainScreen'e geri tuşunda çıkış onay dialogları
- Hakkında ekranına Web + GitHub + Gizlilik linkleri
- Uygulama ikonu yeni logo ile güncellendi (adaptive icon + tüm mipmap yoğunlukları)
- GitHub'a push'landı (commit `ee77aee`)

**Kalanlar:**
- [ ] Gizlilik politikası URL'si AdMob Console'a girilecek
- [ ] Gizlilik politikası URL'si zaten Hakkında ekranında (linkin çalıştığı doğrulanmalı — repo public olunca)
- [ ] Play Console'da kapalı test süreci başlatılır
- [ ] Data Safety formu doldurulur
- [ ] Store listing (açıklama, ekran görüntüleri — `Store Assets/` klasöründe SS/Feature Graphic hazır görünüyor)

---

## Operasyonel Notlar

- **ADB kurulum kuralı:** APK'yı telefona asla otomatik kurma — kurulum komutunu metin olarak ver, kullanıcı kendi çalıştırsın.
- **Kablosuz ADB:** Telefon (S21 FE) adresi dinamik, port sık değişiyor (46881→43221→46165→42157) — bağlantı reddedilirse güncel portu sor.
- **Ekran görüntüsü:** `adb exec-out screencap -p` PowerShell'de değil, Bash aracıyla yönlendirilmeli (PowerShell `>` binary'yi UTF-16'ya çevirip bozuyor).
- **UI sürme:** Uzun zincirli kör `input tap` komutları kullanma — her tap'ten önce ekran görüntüsüyle durumu doğrula, komut başına 1-2 tap.
