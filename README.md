<div align="center">
  <h1>TriviaQuiz 🎯</h1>
  <p><strong>A Turkish Trivia & Knowledge Quiz App</strong><br><em>Türkçe Bilgi Yarışması Uygulaması</em></p>
  <br>

  <img src="reddit_showcase.png" alt="TriviaQuiz Showcase" width="100%">
  <br><br>

  [![Android](https://img.shields.io/badge/ANDROID-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
  [![Kotlin](https://img.shields.io/badge/KOTLIN-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
  [![Jetpack Compose](https://img.shields.io/badge/JETPACK%20COMPOSE-4285F4?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
  [![DataStore](https://img.shields.io/badge/DATASTORE-4285F4?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/topic/libraries/architecture/datastore)
  [![Play Games Services](https://img.shields.io/badge/PLAY%20GAMES%20SERVICES-FFCA28?style=for-the-badge&logo=google&logoColor=black)](https://developers.google.com/games/services)<br>
  [![License](https://img.shields.io/badge/LICENSE-MIT-4c1?style=for-the-badge)](LICENSE)
  [![Version](https://img.shields.io/badge/VERSION-V1.1.1-007EC6?style=for-the-badge)](https://github.com/gokcank/trivia-quiz-app)
</div>

---

## 🇬🇧 English

### Overview
**TriviaQuiz** is a modern, offline-first Android trivia game featuring a large Turkish-language question bank across 13 categories. Players can customize difficulty, question count, and timer mode, track their progress with detailed statistics, complete daily missions for bonus rewards, and compete with friends through Google Play Games achievements and leaderboards.

### Features
* **13 Categories:** Science & Nature, World History, Sports, Technology, Geography, Animals, Literature, and more.
* **Customizable Games:** Choose difficulty (Easy/Medium/Hard), question count (10/15/20), and timed or untimed play.
* **Joker System:** 50:50, extra time, and skip jokers to help players through tough questions.
* **Daily Missions & Streaks:** A new mission each day; completing it grants a joker bonus for the rest of the day, and streaks are tracked across consecutive days.
* **Statistics Dashboard:** Tracks games played, accuracy, best streak, and per-category performance.
* **Google Play Games Integration:** Automatic sign-in, 10 achievements, and 2 leaderboards (Total Correct Answers, Best Streak).
* **Theme Selection:** Dark, Light, or System — with a fully centralized color system across the app.
* **Bilingual-Ready Architecture:** Built with localization in mind (currently Turkish question content).

### Tech Stack
* **Language:** Kotlin
* **UI:** Jetpack Compose, Material 3, Navigation 3
* **Architecture:** MVVM with AndroidViewModel, unidirectional state flow
* **Asynchronous:** Coroutines & Flow
* **Local Storage:** DataStore Preferences (settings, statistics, daily missions)
* **Services:** Google Play Games Services v2 (achievements & leaderboards)
* **Serialization:** kotlinx.serialization
* **Min SDK:** 24 · **Target/Compile SDK:** 36

### Privacy & Security
All secrets (signing keys, Play Games credentials) are kept out of source control via `local.properties` and are never hardcoded. The project builds successfully even without these keys — related features (e.g. Play Games) simply disable themselves, so anyone can clone and build the app.

---

## 🇹🇷 Türkçe

### Genel Bakış
**TriviaQuiz**, 13 kategoride geniş bir Türkçe soru bankasına sahip, modern ve çevrimdışı çalışan bir Android bilgi yarışması uygulamasıdır. Oyuncular zorluk seviyesini, soru sayısını ve süre modunu özelleştirebilir, istatistiklerini takip edebilir, günlük görevlerle bonus kazanabilir ve Google Play Games başarımları/liderlik tabloları üzerinden arkadaşlarıyla yarışabilir.

### Özellikler
* **13 Kategori:** Bilim & Doğa, Dünya Tarihi, Spor, Teknoloji, Coğrafya, Hayvanlar, Edebiyat ve daha fazlası.
* **Özelleştirilebilir Oyunlar:** Zorluk seviyesi (Kolay/Orta/Zor), soru sayısı (10/15/20) ve zamanlı/süresiz mod seçimi.
* **Joker Sistemi:** 50:50, ek süre ve geç jokerleriyle zorlu sorularda yardım.
* **Günlük Görevler & Seriler:** Her gün yeni bir görev; tamamlanınca o gün boyunca joker bonusu kazanılır, üst üste tamamlanan günler seri olarak takip edilir.
* **İstatistik Paneli:** Oynanan oyun sayısı, doğruluk oranı, en iyi seri ve kategori bazlı performans takibi.
* **Google Play Games Entegrasyonu:** Otomatik giriş, 10 başarım ve 2 liderlik tablosu (Toplam Doğru, En İyi Seri).
* **Tema Seçimi:** Koyu, Açık veya Sistem — uygulama genelinde merkezi bir renk sistemiyle.

### Kullanılan Teknolojiler
* **Dil:** Kotlin
* **Arayüz:** Jetpack Compose, Material 3, Navigation 3
* **Mimari:** AndroidViewModel ile MVVM, tek yönlü state akışı
* **Asenkron İşlemler:** Coroutines & Flow
* **Yerel Depolama:** DataStore Preferences (ayarlar, istatistikler, günlük görevler)
* **Servisler:** Google Play Games Services v2 (başarımlar & liderlik tabloları)
* **Serileştirme:** kotlinx.serialization
* **Min SDK:** 24 · **Target/Compile SDK:** 36

### Gizlilik & Güvenlik
Tüm gizli değerler (imza anahtarları, Play Games kimlik bilgileri) `local.properties` aracılığıyla kaynak kod dışında tutulur ve asla doğrudan koda yazılmaz. Proje bu anahtarlar olmadan da sorunsuz derlenir — ilgili özellikler (ör. Play Games) kendini otomatik olarak devre dışı bırakır; böylece depoyu klonlayan herkes uygulamayı derleyebilir.

---

<div align="center">
  <img src="logo.png" alt="TriviaQuiz Logo" width="120">
</div>
