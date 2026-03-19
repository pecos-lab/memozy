#!/bin/bash
PROJECT_ROOT=$(pwd)
RES="$PROJECT_ROOT/app/src/main/res"
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "🚀 Memozy 로고 적용 시작..."
if [ ! -d "$RES" ]; then
  echo "❌ app/src/main/res 없음. 프로젝트 루트에서 실행하세요."
  exit 1
fi

mkdir -p "$RES/drawable"
mkdir -p "$RES/mipmap-anydpi-v26"
mkdir -p "$RES/mipmap-mdpi" "$RES/mipmap-hdpi"
mkdir -p "$RES/mipmap-xhdpi" "$RES/mipmap-xxhdpi" "$RES/mipmap-xxxhdpi"

cp "$DIR/ic_foreground.png" "$RES/drawable/ic_foreground.png"
cp "$DIR/logo_full.png"     "$RES/drawable/logo_full.png"
cp "$DIR/logo_wordmark.png" "$RES/drawable/logo_wordmark.png"

cat > "$RES/drawable/ic_background.xml" << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="#FFFFFF" />
</shape>
EOF
echo "✅ drawable 복사 완료"

cp "$DIR/mipmap_png/ic_launcher_mdpi_48x48.png"      "$RES/mipmap-mdpi/ic_launcher.png"
cp "$DIR/mipmap_png/ic_launcher_hdpi_72x72.png"       "$RES/mipmap-hdpi/ic_launcher.png"
cp "$DIR/mipmap_png/ic_launcher_xhdpi_96x96.png"      "$RES/mipmap-xhdpi/ic_launcher.png"
cp "$DIR/mipmap_png/ic_launcher_xxhdpi_144x144.png"   "$RES/mipmap-xxhdpi/ic_launcher.png"
cp "$DIR/mipmap_png/ic_launcher_xxxhdpi_192x192.png"  "$RES/mipmap-xxxhdpi/ic_launcher.png"
echo "✅ mipmap PNG 복사 완료"

cat > "$RES/drawable/ic_foreground_inset.xml" << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<inset xmlns:android="http://schemas.android.com/apk/res/android"
    android:drawable="@drawable/ic_foreground"
    android:inset="15%" />
EOF

cat > "$RES/mipmap-anydpi-v26/ic_launcher.xml" << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_background"/>
    <foreground android:drawable="@drawable/ic_foreground_inset"/>
</adaptive-icon>
EOF
cat > "$RES/mipmap-anydpi-v26/ic_launcher_round.xml" << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_background"/>
    <foreground android:drawable="@drawable/ic_foreground_inset"/>
</adaptive-icon>
EOF
echo "✅ Adaptive Icon XML 완료"

mkdir -p "$PROJECT_ROOT/playstore_assets"
cp "$DIR/mipmap_png/ic_launcher_playstore_512x512.png" "$PROJECT_ROOT/playstore_assets/ic_launcher_512.png"
echo "✅ 플레이스토어 512px 저장"

echo ""
echo "========================================"
echo "✅ 완료! File → Sync Project with Gradle"
echo "========================================"
