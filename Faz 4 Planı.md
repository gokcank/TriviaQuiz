# Faz 4 Planı — Tema Seçimi, Günlük Görev ve Play Games (v1.1)

**Kapsam:** Tema seçimi (Koyu / Açık / Sistem) · Günlük görev + bonus · Play Games (otomatik giriş, 10 başarım, 2 liderlik tablosu)

**Rafa kaldırılanlar:** "Reklamları kaldır" satın alımı ve coin ekonomisi bu sürüme girmiyor; ikisi de not defterine (scratch_notes.md) işlenecek.

**Çalışma sırası:** Önce tema (4A), sonra günlük görev (4B), en son Play Games (4C). Play Games'teki iki başarım günlük görev verilerini kullandığı için bu sıra zorunlu.

**Güvenlik ilkesi:** Play Games kimlikleri dahil tüm gizli değerler local.properties dosyasında durur, koda yazılmaz. Anahtarlar hiç girilmemiş olsa bile proje sorunsuz derlenir; ilgili özellik kendini gizler. Böylece depoyu GitHub'dan klonlayan herkes uygulamayı derleyebilir.

**Sürüm:** Her şey doğrulandıktan sonra en son adım olarak sürüm 1.1.0'a yükseltilir.

---

## ✅ Adım 0 — Not defterine iki not (tamamlandı, 2026-07-12)

scratch_notes.md dosyasının "Fikirler & Notlar" bölümüne iki madde eklenecek:

- "Reklamları kaldır" tek seferlik satın alımının v1.1 kapsamı dışına alındığı (karar: 12 Temmuz 2026).
- Coin ekonomisi fikrinin rafta beklediği; günlük görev ödülünün şimdilik coin'siz, doğrudan +1 joker olduğu.

---

## ✅ Faz 4A — Tema Seçimi (Koyu / Açık / Sistem) (kod tamamlandı, 2026-07-12 — cihazda görsel test bekliyor)

**Bugünkü durum:** Uygulamada yalnızca koyu tema var ve renkler tek tek ekranların içine gömülü durumda.

**Yapılacaklar:**

- Uygulamanın 16 temel rengi (arka plan, kart, vurgu, metinler, doğru/yanlış/uyarı, altın, sayaç yeşili) tek bir merkezi palette toplanacak. Ekranlar rengi artık bu paletten alacak. Yedi ekran dosyasındaki yaklaşık 184 renk kullanımı bu palete yönlendirilecek — koyu temada görünüm değişmeyecek.
- Mevcut koyu palet birebir korunacak. Yeni bir açık palet tasarlanacak: beyaz-gri zemin, koyu metin; doğru/yanlış/uyarı gibi renkler açık zeminde okunaklı olacak şekilde bir ton koyulaştırıldı. "Başla!" düğmesindeki mavi-mor renk geçişi iki temada da aynı kalacak (üzerindeki beyaz yazı ikisinde de okunuyor).
- Ayarlar ekranının en üstüne "GÖRÜNÜM" bölümü gelecek: 🌙 Koyu / ☀️ Açık / 📱 Sistem seçenekleri, altında "Sistem, cihaz temasını takip eder" açıklaması.
- Seçim kalıcı olarak saklanacak. **Varsayılan koyu** — güncelleme alan mevcut kullanıcılar hiçbir fark görmeyecek.
- Durum çubuğu ve alt gezinme çubuğu ikonlarının rengi seçilen temaya uyacak. Uygulama açılırken görünen ilk pencere rengi de cihaz temasına göre ayarlanacak (açılışta rahatsız edici beyaz/siyah parlama olmaması için).

**Test edilecek kenar durumları:** Ayardan tema değiştirince anında yansıma; Sistem modundayken cihaz temasını değiştirme; uygulamayı kapatıp açınca seçimin korunması; eski kurulumdan güncellemede koyu açılış.

---

## ✅ Faz 4B — Günlük Görev + Bonus (kod tamamlandı, 2026-07-12 — cihazda test bekliyor)

Tamamen cihaz içinde çalışır; internet veya sunucu gerekmez.

**Nasıl işleyecek:**

- Her gün, o güne özel bir görev otomatik seçilir. Seçim gün numarasından türetildiği için aynı gün içinde görev değişmez.
- Altı görev tipi dönüşümlü gelir: 20 soru cevapla · 15 doğru yap · 2 oyun bitir · bir oyunda %80 ve üzeri skor yap · belirli bir kategoride 5 doğru yap · Zor modda 5 doğru yap.
- İlerleme her oyun bittiğinde işlenir. "Geç" jokeriyle atlanan sorular sayılmaz. Kategori görevi, "Tüm Kategoriler" ile oynanan oyunlarda da ilerler çünkü her sorunun gerçek kategorisi biliniyor.
- **Ödül:** Görev tamamlanınca o günün geri kalanındaki tüm oyunlarda üç joker de +1 hakla başlar (10 soruluk oyunda 1+1, 15 ve 20 soruluk oyunda 2+1).
- **Seri:** Üst üste her gün görev tamamlandıkça 🔥 sayacı artar; bir gün atlanırsa sıfırlanır. Bu seri, Play Games'teki "7 gün üst üste" başarımını da besler.

**Arayüz:**

- Ana ekranda, "Bilgini Sına!" yazısı ile kategori seçimi arasına "GÜNLÜK GÖREV" kartı gelir: görev metni, ilerleme çubuğu ve sayaç (örn. 8/15). Görev tamamlanınca kart altın kenarlıkla "✓ Tamamlandı — bugünkü oyunlarda tüm jokerler +1!" gösterir; seri 2 güne ulaştıysa "🔥 x2 gün" rozeti eklenir.
- Görev o oyunda tamamlandıysa sonuç ekranında "🎁 Günlük görev tamamlandı!" kutlama satırı görünür.

**Test edilecek kenar durumları:** Gece yarısını aşan oyun (bonus oyun başında belirlendiği için korunur); cihaz saatinin ileri/geri alınması (gün değiştiyse yeni görev üretilir); ilk kurulumda serinin sıfırdan başlaması.

---

## ✅ Faz 4C — Play Games Services (kod tamamlandı, 2026-07-13 — Gökcan'ın konsol görevleri + kimlikli test bekliyor)

**Nasıl işleyecek:**

- **Otomatik giriş:** Uygulama açılınca Google Play Games girişi kendiliğinden önerilir. Reddedilirse İstatistikler ekranından elle "Giriş Yap" yapılabilir.
- **Geçmiş aktarılır:** İlk başarılı girişte, o ana kadar cihazda birikmiş istatistikler (oyun sayısı, doğru sayısı, en iyi seri) Play Games'e bir kerede aktarılır. Girişten önce oynanmış oyunlar kaybolmaz.
- **Çevrimdışı güvenli:** İnternet yokken oyun bitirmek hata veya çökme üretmez; eksik kalan ne varsa sonraki girişte senkron tamamlar.
- **Anahtarsız çalışma:** Kimlikler local.properties dosyasında yoksa uygulama normal derlenir ve Play Games ile ilgili her şey gizlenir.

**Arayüz:** İstatistikler ekranının en üstüne "PLAY GAMES" kartı gelir. Girişliyse 🏆 Başarımlar ve 🥇 Liderlik Tabloları düğmeleri, girişsizse kısa açıklama + "Giriş Yap" düğmesi.

**Liderlik tabloları (2):** Toplam Doğru · En İyi Seri.

**Başarımlar (10):**

| Ad | Koşul |
|---|---|
| İlk Adım | İlk oyununu bitir |
| Azimli | 10 oyun bitir (ilerlemeli) |
| Quiz Tutkunu | 50 oyun bitir (ilerlemeli) |
| Bilgi Küpü | Toplam 100 doğru (ilerlemeli) |
| Bilgelik Hazinesi | Toplam 500 doğru (ilerlemeli) |
| Kusursuz | Bir oyunda %100 skor yap |
| Seri Ustası | Tek oyunda 15 doğru serisi yakala |
| Zorlu Rakip | Zor modda %80 ve üzeri skor yap |
| Günün Görevlisi | İlk günlük görevini tamamla |
| İstikrar Abidesi | 7 gün üst üste görev tamamla |

### Senin yapacakların (Play Console tarafı)

1. Play Console → Grow → Play Games Services kurulumu: proje oluşturma, Cloud projesine bağlama, OAuth onay ekranını yayınlama.
2. Android OAuth istemcisi: paket adı + üç SHA-1 parmak izi (debug, upload ve Play App Signing anahtarları).
3. 10 başarımı ve 2 liderlik tablosunu konsolda oluşturmak. Her başarım için 512×512 PNG ikon gerekiyor — istersen ikonları ben üretirim (ayrı mini görev).
4. Uygulama kimliği ile 12 başarım/tablo kimliğini local.properties dosyasına eklemek (anahtar adlarını implementasyon sırasında hazır liste olarak vereceğim).
5. Kendini tester olarak eklemek; testler bitince Play Games yapılandırmasını yayınlamak.

---

## Doğrulama (test planı)

1. **Derleme:** Hem tüm anahtarlar doluyken hem de Play Games anahtarları local.properties'ten çıkarılmışken proje derlenmeli. Anahtarsız derlemede uygulama normal açılmalı, Play Games hiç görünmemeli.
2. **Tema:** Üç mod arasında geçiş; Sistem modunda cihaz temasını değiştirme (durum çubuğu ikonları dahil); tüm ekranların, pencerelerin ve açılır menülerin açık temada okunaklılığı; kapat-aç kalıcılığı.
3. **Günlük görev:** İlerleme ve tamamlanma akışı; bonus sonrası joker sayıları (10 soruda 2-2-2, 15 ve üzeri soruda 3-3-3); cihaz tarihini bir gün ileri alınca yeni görev + seri artışı, iki gün ileri alınca serinin sıfırlanması; 23:59'da başlayan oyun.
4. **Play Games:** Otomatik giriş; Başarımlar ve Liderlik ekranlarının açılması; ilk başarım bildirimi ve skor gönderimi; uçak modunda oyun bitirme (çökme yok, sonraki girişte senkron).
5. Aynı testlerin küçültülmüş yayın (release) sürümüyle tekrarı. APK'yı telefona kurma komutu metin olarak verilecek, otomatik kurulum yapılmayacak.

## Sürüm güncellemesi (en son adım)

Tüm doğrulamalar bittikten sonra sürüm 1.1.0'a yükseltilir ve Play Console için AAB + test için APK üretilir.

## Not

Bu dosya yerel çalışma belgesidir; repoya commit edilmez.
