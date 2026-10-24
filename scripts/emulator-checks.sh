#!/usr/bin/env bash
set -euo pipefail
apk_path="$1"
evidence_dir="$2"
mkdir -p "$evidence_dir"
restore_network() {
  adb shell svc wifi enable || true
  adb shell svc data enable || true
}
trap restore_network EXIT

save_reports() {
  local label="$1"
  mkdir -p "$evidence_dir/$label"
  if [ -d app/build/reports/androidTests/connected ]; then
    cp -R app/build/reports/androidTests/connected "$evidence_dir/$label/html"
  fi
  if [ -d app/build/outputs/androidTest-results/connected ]; then
    cp -R app/build/outputs/androidTest-results/connected "$evidence_dir/$label/results"
  fi
}

wait_mode() {
  local expected="$1"
  for attempt in $(seq 1 20); do
    adb shell uiautomator dump /sdcard/kitchen-window.xml >/dev/null
    adb pull /sdcard/kitchen-window.xml "$evidence_dir/window.xml" >/dev/null
    if python3 - "$evidence_dir/window.xml" "$expected" <<'PY'
import sys,xml.etree.ElementTree as ET
root=ET.parse(sys.argv[1]).getroot()
text=' '.join(node.get('text','') for node in root.iter())
expected={'live':('Live website · latest content','Canlı site · güncel içerik'), 'offline':('Offline snapshot · 80 recipes','Çevrimdışı kopya · 80 tarif')}[sys.argv[2]]
sys.exit(0 if any(value in text for value in expected) else 1)
PY
    then
      return 0
    fi
    sleep 1
  done
  echo "Expected $expected mode did not become ready" >&2
  return 1
}

set +e
./gradlew --no-daemon --max-workers=2 connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.student.recipes.RecipeAppTest
online_result=$?
set -e
save_reports online-tests
if [ "$online_result" -ne 0 ]; then exit "$online_result"; fi
adb install -r "$apk_path"
adb shell am start -W -n com.oyku.akillimutfak/com.student.recipes.MainActivity
wait_mode live
adb shell screencap -p /sdcard/kitchen-live.png
adb pull /sdcard/kitchen-live.png "$evidence_dir/live-website.png"

# The host retains network access for Gradle; only the Android emulator is disconnected.
adb shell svc wifi disable
adb shell svc data disable
set +e
./gradlew --no-daemon --max-workers=2 connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.student.recipes.OfflineFallbackTest
offline_result=$?
set -e
save_reports offline-tests
if [ "$offline_result" -ne 0 ]; then exit "$offline_result"; fi
adb install -r "$apk_path"
adb shell am start -W -n com.oyku.akillimutfak/com.student.recipes.MainActivity
wait_mode offline
adb shell screencap -p /sdcard/kitchen-offline.png
adb pull /sdcard/kitchen-offline.png "$evidence_dir/offline-80-recipes.png"
cp "$evidence_dir/live-website.png" "$GITHUB_WORKSPACE/emulator-screenshot.png"
restore_network
