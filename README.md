# Smart Kitchen / Akıllı Mutfak — Android 2.0

Android Studio kullanmadan GitHub Actions ile derlenen Android uygulaması. Paket kimliği `com.oyku.akillimutfak`, minimum Android 6.0, hedef Android 15.

## Task 4 — Extra point

- **Tasarım:** Yeni yaprak logosu, yeşil/krem renk paleti, özgün yemek çizimleri, mobil tarif kartları, İngilizce/Türkçe seçenekleri ve makro besin değerleri uygulamaya eklendi. Tasarım kodla oluşturulmuştur; Canva kullanıldığı iddia edilmez.
- **Canlı site entegrasyonu:** Uygulama açıldığında `https://oyku521.github.io/gastronomy-task3-yaz./` adresini kendi WebView penceresinde açar. İnternet varken yeni site içeriği sonraki açılışta veya **↻ Live / Canlı** düğmesiyle alınır; web içeriği değişiklikleri için yeni APK gerekmez. Bu düğme dış tarayıcı açmaz.
- **Çevrimdışı yedek:** Ağ hatası, HTTP hatası, TLS hatası veya 15 saniyelik yükleme zaman aşımında cihazdaki 80 tariflik kopyaya geçilir. **Offline / Çevrimdışı** düğmesi bu kopyayı doğrudan açar. Sertifika doğrulaması kapatılmaz.

Canlı ve çevrimdışı mod yerel depolamaları farklı web kökenlerindedir; favoriler, dil ve malzeme seçimleri modlar arasında otomatik eşitlenmez. Bu özellik içerik güncelliği sağlar, kullanıcı hesabı/veri senkronizasyonu değildir. Cihazdaki kopya APK ile sabittir; yenilemek için kaynak kopyayı güncellemek ve yeni APK derlemek gerekir. Canlı site gelecekte değişirse tarif sayısı 80'in üzerine çıkabilir.

Android WebView isteklerinde `SmartKitchenAndroid/2.0` işareti kullanılır. Website bu işareti görünce PWA yükleme düğmesini ve service worker kaydını kapatır; böylece canlı mod güncel HTTPS içeriğini alır. Normal web tarayıcılarında sitenin PWA/çevrimdışı özelliği korunur. Uygulama canlı modda HTTP önbelleğini kullanmaz. Sadece kendi HTTPS site yolu ve yerel varlık kökeni uygulama içinde açılır; diğer web bağlantıları harici tarayıcıya gider. JavaScript-native köprüsü eklenmemiştir.

## 80 tariflik içerik

İngilizce/Türkçe klasik yemekler, vegan ve glutensiz malzemeli seçenekler; tek malzemeyle öneri, kombinlenebilir diyet filtreleri, favoriler, porsiyon ayarı, gramajlar, hazırlanış ve yaklaşık besin değerleri. Tarif verileri `app/src/main/assets/recipes.json`, malzeme tahminleri `food-catalog.json` içinde koddan ayrı tutulur. Kalori ve makrolar genel editoryal tahminlerdir; doğrulanmış klinik/marka verisi değildir. Glutensiz malzeme etiketi çölyak güvenliği sertifikası değildir; sertifikalı ürün ve çapraz temas kontrolü gerekir.

## APK ve emülatör kanıtları

[GitHub Actions](https://github.com/oyku521/akilli-mutfak-android/actions) → **Build Android APK** → son başarılı çalışma → **Artifacts**:

- **Akilli-Mutfak-APK:** ZIP içindeki `app-debug.apk` Android telefona kurulur.
- **Android-emulator-evidence:** `live-website.png`, `offline-80-recipes.png`, `emulator-screenshot.png` ve test raporları. Canlı site testi uygulamanın gerçek GitHub Pages adresini açtığını doğrular. İkinci test turunda yalnızca emülatörün Wi-Fi ve mobil verisi kapatılır; yerel kopyaya otomatik geçiş doğrulanır ve ağ tekrar açılır.
- **Android-lint-report:** Statik Android analiz raporu.

Bu eğitim amaçlı debug APK'dır. Yeni GitHub koşucusunun debug imzası eski kurulumdan farklı olabilir. İmza uyuşmazlığında eski uygulamayı kaldırıp yeni APK'yı kurmak gerekir; kaldırma cihazdaki eski favorileri siler. Play Store sürümü ve kalıcı imza anahtarı bu ödevin kapsamı değildir.

Word'e mevcut zorunlu kanıtların yanında şu açıklama eklenebilir:

> Extra work: I improved the app with an original logo, coordinated colors, recipe illustrations and an English/Turkish interface. I embedded my live GitHub Pages website inside the Android app, so published website changes appear when the app opens online or when Live is refreshed, without rebuilding the APK. If the website is unreachable, the app automatically falls back to an offline snapshot containing 80 recipes. Live and offline emulator screenshots demonstrate both modes.

Ekran görüntülerini gerçek başarılı çalışmadan alın. Telefon videosu gerçek Android telefonda çekilmelidir. GitHub emülatör ekran görüntüsü, öğretmenin ayrıca istediği Appetize gibi tarayıcıda etkileşimli emülatörün yerine geçtiği iddiasıyla kullanılmamalıdır. Yeni APK Appetize'a yüklenerek güncel uygulamanın görüntüsü alınabilir.

## Kaynak kopyayı yenileme

Kardeş web deposu aynı çalışma alanında mevcutken:

```sh
python3 scripts/update-bundled-site.py --website ../gastronomy-task3-yaz.
```

Bu işlem ağ erişimi kullanmadan web arayüzü, JSON verileri, logo ve çizimleri APK'nın yerel varlıklarına kopyalar. Web sitesi uygulamadan bağımsız olarak yayınlanabilir. Mevcut check-out yeterlidir; ek Git worktree gerekmez.

## Derleme ve test

Java 17, Gradle 8.9, Android Gradle Plugin 8.7.3, Kotlin 2.0.21 ve SDK/Build Tools 35 kullanılır. Gradle wrapper dağıtımı SHA-256 ile doğrulanır.

```sh
./gradlew --no-daemon --max-workers=2 assembleDebug lintDebug assembleDebugAndroidTest
```

Bu komut test APK'sını derler; cihaz testlerini çalıştırmaz. Bağlı emülatörde normal ağ ile:

```sh
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.student.recipes.RecipeAppTest
```

`OfflineFallbackTest` yalnızca ağı kapalı **test emülatöründe** çalıştırılmalıdır. `scripts/emulator-checks.sh` GitHub emülatörü için iki test turunu, ekran görüntülerini ve ağın geri açılmasını yönetir. Gerçek kişisel telefonda bu betiği kullanmayın.
