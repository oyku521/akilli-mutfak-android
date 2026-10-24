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

remove_installed_app() {
  local installed_path
  if installed_path="$(adb shell pm path com.oyku.akillimutfak)" && [[ "$installed_path" == package:* ]]; then
    adb uninstall com.oyku.akillimutfak
  fi
}

install_screenshot_apk() {
  # Gradle and the build-artifact job have different debug signing keys.
  # A clean install works whether connected tests retain or remove their target app.
  remove_installed_app
  adb install "$apk_path"
}

save_reports() {
  local label="$1"
  mkdir -p "$evidence_dir/$label"
  if [ -d app/build/reports/androidTests/connected ]; then
    cp -R app/build/reports/androidTests/connected "$evidence_dir/$label/html"
  fi
  if [ -d app/build/outputs/androidTest-results/connected ]; then
    cp -R app/build/outputs/androidTest-results/connected "$evidence_dir/$label/results"
  fi
  python3 - "$evidence_dir/$label" "$label" <<'PYREPORT'
import os,sys,xml.etree.ElementTree as ET
from pathlib import Path
files=list(Path(sys.argv[1]).glob('results/**/TEST-*.xml'))
suites=[ET.parse(file).getroot() for file in files]
count=sum(int(s.get('tests','0')) for s in suites)
failures=sum(int(s.get('failures','0')) + int(s.get('errors','0')) for s in suites)
lines=[f"### {sys.argv[2]}",f"Executed tests: {count}; failures/errors: {failures}"]
for suite in suites:
 for case in suite.iter('testcase'):
  for failure in list(case.findall('failure'))+list(case.findall('error')):
   lines += [f"#### {case.get('name','test')}","```text",failure.text or failure.get('message','Failure'),"```"]
summary=os.environ.get('GITHUB_STEP_SUMMARY')
if summary:
 with open(summary,'a') as out:out.write('\n'.join(lines)+'\n')
print('\n'.join(lines))
if count == 0:
 raise SystemExit('No executed instrumentation tests found in current XML results')
PYREPORT
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

# Only current-run reports may be counted as evidence.
rm -rf app/build/outputs/androidTest-results/connected app/build/reports/androidTests/connected
set +e
./gradlew --no-daemon --max-workers=2 connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.student.recipes.RecipeAppTest
online_result=$?
set -e
save_reports online-tests
if [ "$online_result" -ne 0 ]; then exit "$online_result"; fi
install_screenshot_apk
adb shell am start -W -n com.oyku.akillimutfak/com.student.recipes.MainActivity
wait_mode live
adb shell screencap -p /sdcard/kitchen-live.png
adb pull /sdcard/kitchen-live.png "$evidence_dir/live-website.png"

# The screenshot APK was signed by a different GitHub job. Remove it before Gradle
# installs its own test-signed build; otherwise the second install can reject its signature.
remove_installed_app
# The host retains network access for Gradle; only the Android emulator is disconnected.
adb shell svc wifi disable
adb shell svc data disable
rm -rf app/build/outputs/androidTest-results/connected app/build/reports/androidTests/connected
set +e
./gradlew --no-daemon --max-workers=2 --rerun-tasks connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.student.recipes.OfflineFallbackTest
offline_result=$?
set -e
save_reports offline-tests
if [ "$offline_result" -ne 0 ]; then exit "$offline_result"; fi
install_screenshot_apk
adb shell am start -W -n com.oyku.akillimutfak/com.student.recipes.MainActivity
wait_mode offline
adb shell screencap -p /sdcard/kitchen-offline.png
adb pull /sdcard/kitchen-offline.png "$evidence_dir/offline-80-recipes.png"
cp "$evidence_dir/live-website.png" "$GITHUB_WORKSPACE/emulator-screenshot.png"
restore_network
