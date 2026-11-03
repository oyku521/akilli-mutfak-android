#!/usr/bin/env bash
set -euo pipefail
mkdir -p evidence
adb shell settings put system accelerometer_rotation 0
adb shell settings put system user_rotation 0
adb shell wm size 1080x1920
adb shell wm density 420
adb shell input keyevent 82
./gradlew --no-daemon --max-workers=2 assembleDebug assembleDebugAndroidTest
adb shell screenrecord --size 720x1280 --bit-rate 2000000 --time-limit 180 /sdcard/mezze-gameplay.mp4 &
RECORD_PID=$!
finish() {
  adb shell pkill -2 screenrecord || true
  wait "$RECORD_PID" || true
  adb pull /sdcard/mezze-gameplay.mp4 evidence/mezze-gameplay.mp4 || true
  adb pull /sdcard/Android/data/com.oyku.mezzemaster/files/evidence/. evidence/ || true
  adb logcat -d -s AndroidRuntime > evidence/android-runtime.log || true
}
trap finish EXIT
./gradlew --no-daemon --max-workers=2 connectedDebugAndroidTest
