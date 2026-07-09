# TriviaQuiz — Faz 3 Test Kontrol Listesi

Telefona debug APK'yı yüklemek için:
```
adb install -r "app\build\outputs\apk\debug\app-debug.apk"
```

---

## 1. Uygulama Açılışı

- [X] Uygulama çökmeden açılıyor
- [ ] Logcat'te AdMob init hatası yok (`MobileAds.initialize` tamamlandı)
- [X] UMP consent formu görünüyor **veya** sessizce geçiyor (TR'de genelde form çıkmaz)

---

## 2. Banner Reklamı (Ana Ekran)

- [X] Ana ekranın alt kısmında test banner'ı görünüyor
- [X] Banner, içeriklerin üstüne binmiyor (kaydırma düzgün çalışıyor)
- [X] İnternet yokken uygulama çökmüyor (banner sadece yüklenmez)

---

## 3. Interstitial Reklamı (3. Oyun Sonrası)

- [X] 1. oyun bitti → interstitial **çıkmıyor**
- [X] 2. oyun bitti → interstitial **çıkmıyor**
- [X] 3. oyun bitti → test interstitial ekranı görünüyor
- [X] Interstitial kapatıldıktan sonra Sonuç ekranı düzgün gösteriliyor
- [X] 4-5-6. oyunlarda döngü devam ediyor (6. oyunda tekrar çıkıyor)

---

## 4. Rewarded Joker Reklamı

- [X] Jokerleri tamamen bitir (50:50 + Süre + Geç)
- [X] Hak 0'a düşünce butonlar **📺 moduna** geçiyor (turuncu renk, "+1" göstergesi)
- [X] 📺 butonuna tıklayınca test rewarded reklam açılıyor
- [X] Ödül alınınca (videoyu izle) ilgili joker **+1 hak** kazanıyor
- [X] Rewarded reklam açıkken **sayaç duruyor** (zamanlı modda test et)
- [X] Reklam kapatıldıktan sonra **sayaç kaldığı yerden devam ediyor**
- [X] Reklam yüklü değilken 📺 butonu **devre dışı** görünüyor

---

## 5. Quiz Akışı (Genel)

- [X] Zamanlı mod: süre dolunca cevap açıklanıyor, sonraki soruya geçiyor
- [X] Süresiz mod: süre göstergesi yok, soru geçilmiyor (bekleniyor)
- [X] 50:50 joker: 2 yanlış şık kayboluyor
- [X] Süre jokeri: +15 sn ekleniyor
- [X] Geç jokeri: soru atlanıyor, seri korunuyor
- [X] Seri sayacı doğru artıyor/sıfırlanıyor

---

## 6. Sonuç Ekranı

- [X] Skor, yüzde ve animasyon doğru gösteriliyor
- [ ] "Tekrar Oyna" → aynı kategori/zorlukla yeni oyun başlıyor (Aynı seçeneklerle başlamıyor, seçim ekranına dönüyor.)
- [X] "Paylaş" → sistem paylaşım menüsü açılıyor
- [X] "Ana Menü" → ana ekrana dönüyor

---

## 7. Ayarlar ve İstatistik

- [X] Ses açık/kapalı → quiz sırasında ses çalıyor/çalmıyor
- [X] Titreşim açık/kapalı → yanlış cevabında titreşim var/yok
- [X] İstatistik ekranında oyun sayısı artıyor
- [X] Zamanlayıcı süresi değiştirince quiz'de yeni süre uygulanıyor

---

## 8. Sıradaki Adımlar (Deferred)

Testler tamamlanıp onaylandıktan sonra:

- [X] AdMob hesabını oluştur ve gerçek uygulama ID'sini al
- [X] `local.properties`'teki `#ADMOB_*` satırlarının başındaki `#` kaldırılarak gerçek ID'ler girilir
- [ ] Gizlilik politikası URL'si AdMob Console ve Hakkında ekranına eklenir
- [ ] Play Console'da kapalı test süreci başlatılır
- [ ] `keystore/release.jks` dosyası güvenli yerde yedeklenir
- [ ] GitHub'a push (`.gitignore` keystore ve `local.properties`'i dışlıyor ✓)
