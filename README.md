# Lebanese Mezze: Plate Master

An English-language, offline Android cooking game built with Kotlin and Jetpack Compose in Android Studio.

## Download and run
Open the latest successful [Actions run](https://github.com/oyku521/akilli-mutfak-android/actions?query=branch%3Alebanese-mezze).
Download **Lebanese-Mezze-APK**, unzip and install app-debug.apk on an Android device or drag it onto an Android Studio emulator (Android 8+).
Download **Android-emulator-video-and-screenshots** for a real automated emulator gameplay video and screenshots.

## Android Studio
Clone this repository, select the **lebanese-mezze** branch, open the project folder, sync Gradle and Run app.
Java 17, Gradle 8.9, Android Gradle Plugin 8.7.3, Kotlin 2.0.21, compile SDK 35.
Official IDE: https://developer.android.com/studio
The main branch retains the previous Smart Kitchen project; this branch is a separate game with its own application ID.

## Game
Five guests request two Lebanese mezze plus a calorie range and minimum protein.
Read recipes, select correct ingredients, prepare servings and balance the plate.
A plate holds up to six servings. Tap a serving to remove it.
All ten dishes have original programmatic illustrations, recipes, serving sizes and calories/protein/carbohydrates/fat.
Nutrition is estimated per serving and varies by preparation, especially frying.

**Recipes:** Hummus, Baba Ghanoush, Tabbouleh, Fattoush, Falafel, Labneh, Muhammara, Warak Enab, Kibbeh and Batata Harra.

## Scoring
Each requested recipe present on the plate: 20 points, maximum 40.
Calories within the requested range: 30.
Protein at or above the requested minimum: 20.
Serve within 90 seconds: 10.
Every rejected ingredient attempt subtracts 5. Round score cannot drop below zero.
Repeated portions do not multiply requested-recipe points.
After 90 seconds, play continues without the time bonus. The clock keeps running in the background.
Five orders yield at most 500. Best score persists on the device. Rotation preserves the active game.

## Verification
```sh
./gradlew assembleDebug testDebugUnitTest lintDebug assembleDebugAndroidTest
./gradlew connectedDebugAndroidTest
```
Unit tests verify achievable nutrition goals, scoring, penalties, ingredient matching and portion totals.
The emulator test plays all five orders, checks wrong-ingredient handling, verifies nutrition totals, rotates/recreates the activity and checks the final saved score.
GitHub Actions records this test on an actual Android emulator; the recording is an automated demonstration.

## Exam submission
Submit a single .docx containing:
1. Repository: https://github.com/oyku521/akilli-mutfak-android/tree/lebanese-mezze
2. A screenshot of the successful GitHub Actions run with its green checkmark (capture the real completed run).
3. YouTube URL of the emulator gameplay recording. Download the MP4 artifact and upload it to your own YouTube account; verify that the teacher can open the link.

The emulator screenshots supplement the video and do not replace either the video or the Actions screenshot.
