# Akıllı Mutfak — Android APK

Hocanın Task 3 sitesi için Android uygulaması. Android Studio gerektirmez; GitHub Actions APK üretir. Tasarım mevcut Akıllı Mutfak sitesinden gelir. 50 tarif `app/src/main/assets/recipes.json` dosyasında tutulur. Uygulama bunları Android WebViewAssetLoader üzerinden yerel olarak açar, dolayısıyla ilk çalıştırma dahil internet gerektirmez. Favoriler cihazda saklanır.

## Yeni GitHub deposu

Bu klasörün **içeriğini**, üstte `android-app` klasörü olmadan yeni deponun köküne koyun. Hazır kaynak ZIP'i `.github/workflows/build.yml` dosyasını da içerir. Mevcut web deposundaki `pages.yml` dosyasını Android deposuna taşımayın.

Gerekli düzen:

```
.github/workflows/build.yml
app/build.gradle.kts
app/src/main/AndroidManifest.xml
app/src/main/java/com/student/recipes/MainActivity.kt
app/src/main/assets/recipes.json
app/src/main/assets/index.html
app/src/main/assets/app.js
app/src/main/assets/style.css
app/src/main/res/layout/activity_main.xml
app/src/main/res/values/{strings,colors,themes}.xml
app/src/main/res/mipmap/ic_launcher.png
gradle/wrapper/{gradle-wrapper.jar,gradle-wrapper.properties}
gradlew
gradlew.bat
build.gradle.kts
settings.gradle.kts
gradle.properties
```

## APK indirme

1. Dosyaları yeni deponun `main` dalına gönderin.
2. **Actions → Build Android APK** iş akışını açın. İlk gönderim derlemeyi başlatır. Gerekirse **Run workflow** seçin.
3. Yeşil tikli tamamlanan çalışmanın **Artifacts → Akilli-Mutfak-APK** dosyasını indirin ve ZIP'i açın.
4. `app-debug.apk` dosyasını Android telefona aktarın. APK'yı açın, gerekirse dosyayı açtığınız uygulamaya “Bu kaynaktan yüklemeye izin ver” iznini verin. Kurulum tamamlanınca bu izni kapatabilirsiniz.
5. Bu eğitim amaçlı debug imzalı APK'dır; Play Store yayını için hazırlanmamıştır. GitHub'ın geçici koşucularında debug imzası değişebilir: güncellemede imza uyuşmazlığı olursa eski sürümü kaldırmak gerekebilir; bu işlem yerel favorileri siler. Sürüm yayınlamak için kalıcı güvenli imzalama ayrı olarak yapılandırılmalıdır.

Java 17, Gradle 8.9, Android Gradle Plugin 8.7.3, Kotlin 2.0.21, Android SDK 35 kullanılır. Minimum Android 6.0 (API 23). Wrapper dağıtımı SHA-256 ile doğrulanır. Başarılı derlemede Android lint, APK imzası, DEX/manifest varlığı ve APK içindeki 50 JSON tarif kontrol edilir.

## Emülatör ve teslim kanıtları

Her `main` gönderiminde APK derlemesinin ardından Android 15 emülatör testleri de çalışır. Ayrıca **Actions → Build Android APK → Run workflow → run_emulator: true** ile tekrar başlatılabilir. İş akışı JSON veri ve gerçek WebView arama/vegan filtresi testlerini çalıştırıp **Android-emulator-evidence** çıktısına gerçek emülatör ekran görüntüsünü ekler. Bu bir tarayıcı içinde etkileşimli emülatör değildir.

Hocanın tarayıcı içinde etkileşimli emülatör şartı için APK'yı [Appetize](https://appetize.io/) hesabına yükleyip Android oturumunda çalıştırın. Kullanım/hesap koşullarını hizmetten kontrol edin. Çalışan uygulamanın ekran görüntüsünü alın; hesap ve dış hizmete yükleme işlemi kullanıcı tarafından yapılmalıdır. API anahtarı projede gerekmez.

Word teslimine yeni depo bağlantısını, gerçek başarılı Actions ekran görüntüsünü, gerçek Android telefonda uygulama videosunun bağlantısını ve çevrimiçi emülatör ekran görüntüsünü ekleyin. Video için uygulamayı açın, Yumurta/Domates/Biber seçin, Menemen sonucunu gösterin, favoriye ekleyin ve internetsiz yeniden açın. Video bağlantısının hocanın erişimine açık olduğunu kontrol edin.

## Kod ve site ilişkisi

Web arayüzü APK içinde paketlenir. “Web sitesini aç” düğmesi canlı siteyi harici tarayıcıda açar. Bu sürüm canlı siteden otomatik içerik eşitlemez; eşitleme yönergelerde isteğe bağlıdır. Tarif değişikliklerinde `recipes.json` dosyasını güncelleyip yeni APK derleyin. APK içinde PWA kurulum düğmesi veya service worker yoktur; çevrimdışı çalışma Android varlıklarıyla sağlanır.

## Yerel komutlar (Android Studio yok)

JDK 17 ve Android SDK 35 bulunan bir terminalde:

```
./gradlew --no-daemon --max-workers=2 assembleDebug lintDebug
./gradlew connectedDebugAndroidTest
```

İkinci komut bağlı Android cihazı veya çalışan Android emülatörü gerektirir. Derlenen APK: `app/build/outputs/apk/debug/app-debug.apk`. `local.properties`, derleme önbellekleri ve imzalama anahtarları depoya eklenmez.
