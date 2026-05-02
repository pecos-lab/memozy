#!/bin/bash
set -e

UDID="49B46EBC-C773-439F-9A3A-35469BDF8FE9"  # iPhone 17 Pro
BUNDLE_ID="me.pecos.memozy.ios"
PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"

echo "▶ 1/3 Gradle KMM 프레임워크 빌드..."
cd "$PROJECT_ROOT"
./gradlew :shared:umbrella:linkDebugFrameworkIosSimulatorArm64

echo "▶ 2/3 Xcode 빌드..."
xcodebuild \
  -project "$PROJECT_ROOT/iosApp/iosApp.xcodeproj" \
  -scheme iosApp \
  -destination "id=$UDID" \
  -configuration Debug \
  build

echo "▶ 3/3 시뮬레이터 실행..."
xcrun simctl boot "$UDID" 2>/dev/null || true
open -a Simulator

APP_PATH=$(find ~/Library/Developer/Xcode/DerivedData/iosApp-*/Build/Products/Debug-iphonesimulator -name "iosApp.app" -maxdepth 1 2>/dev/null | head -1)
xcrun simctl install "$UDID" "$APP_PATH"
xcrun simctl launch "$UDID" "$BUNDLE_ID"

echo "✅ iOS 앱 실행 완료"
