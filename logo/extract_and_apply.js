/**
 * Memozy 로고 SVG → PNG 추출 및 Android 리소스 적용 스크립트
 *
 * SVG 구조:
 *  - memozy_main/memozy_m/memozy_text: 같은 500x500 PNG를 각기 다른 영역 크롭
 *  - memozy_logo: 별도 500x500 PNG (풀 로고)
 *
 * 크롭 좌표 (SVG transform 역산):
 *  - memozy_main (350x350): (74, 74, 350, 350)
 *  - memozy_m    ( 66x 66): (93, 300, 66, 66)  ← M 아이콘
 *  - memozy_text (350x 88): (74, 290, 350, 88) ← 텍스트
 *  - memozy_logo (200x200): full 500x500       ← 별도 이미지
 */

const g = require('child_process').execSync('npm root -g').toString().trim();
const sharp = require(g + '/sharp');
const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const RES  = path.join(ROOT, 'app/src/main/res');
const MV2  = path.join(ROOT, 'Memozy_v2');

function extractPng(svgPath) {
  const content = fs.readFileSync(svgPath, 'utf8');
  const match = content.match(/xlink:href="data:image\/png;base64,([^"]+)"/);
  if (!match) throw new Error('No PNG in ' + svgPath);
  return Buffer.from(match[1], 'base64');
}

// SVG transform 역산으로 구한 크롭 좌표
const CROPS = {
  icon: { src: 'memozy_m',    left: 93,  top: 300, width: 66,  height: 66  }, // M 아이콘
  main: { src: 'memozy_main', left: 74,  top: 74,  width: 350, height: 350 }, // 정사각 로고
  text: { src: 'memozy_text', left: 74,  top: 290, width: 350, height: 88  }, // 텍스트
  logo: { src: 'memozy_logo', left: 0,   top: 0,   width: 500, height: 500 }, // 풀 로고 (별도 PNG)
};

// mipmap 크기 (foreground는 108dp 캔버스에 올라감)
const MIPMAP_SIZES = [
  { dir: 'mipmap-mdpi',    size: 48  },
  { dir: 'mipmap-hdpi',    size: 72  },
  { dir: 'mipmap-xhdpi',   size: 96  },
  { dir: 'mipmap-xxhdpi',  size: 144 },
  { dir: 'mipmap-xxxhdpi', size: 192 },
];

// adaptive icon foreground 캔버스: 108dp, 이미지는 safe zone(66dp) 안에 배치
// → 108px 캔버스에 66px 이미지를 중앙 배치
const ADAPTIVE_CANVAS = 432; // xxxhdpi 기준 실제 px (192 * 2.25 ≈ 432)
const ADAPTIVE_ICON_SIZE = 192 * 3; // 576px foreground 소스

async function run() {
  console.log('📦 PNG 추출 중...');

  // 1) SVG에서 PNG 버퍼 추출
  const bufs = {};
  for (const [key, crop] of Object.entries(CROPS)) {
    const svgPath = path.join(__dirname, crop.src + '.svg');
    const rawBuf = extractPng(svgPath);
    const cropBuf = await sharp(rawBuf)
      .extract({ left: crop.left, top: crop.top, width: crop.width, height: crop.height })
      .png()
      .toBuffer();
    bufs[key] = cropBuf;
    console.log(`  ✅ ${key}: ${crop.width}x${crop.height} 추출`);
  }

  // 2) drawable 리소스로 복사
  console.log('\n🖼️  drawable 배포...');
  const drawableDir = path.join(RES, 'drawable');
  fs.mkdirSync(drawableDir, { recursive: true });

  // 로고 전체 (풀 로고)
  await sharp(bufs.logo).resize(512, 512).png().toFile(path.join(drawableDir, 'logo_full.png'));
  // 정사각 아이콘 로고
  await sharp(bufs.main).resize(512, 512).png().toFile(path.join(drawableDir, 'logo_icon.png'));
  // 텍스트 (wordmark)
  await sharp(bufs.text).resize(500, 125).png().toFile(path.join(drawableDir, 'logo_wordmark.png'));
  console.log('  ✅ logo_full.png, logo_icon.png, logo_wordmark.png');

  // 3) adaptive icon foreground 생성
  //    108dp 캔버스에 M 아이콘을 안전 영역(61%) 안에 배치
  console.log('\n🎯 Adaptive Icon foreground 생성...');
  const FG_CANVAS = 576;
  const FG_ICON   = Math.round(FG_CANVAS * 0.60); // safe zone ~60%
  const FG_OFFSET = Math.round((FG_CANVAS - FG_ICON) / 2);

  const fgBuf = await sharp(bufs.icon)
    .resize(FG_ICON, FG_ICON)
    .png()
    .toBuffer();

  const foregroundBuf = await sharp({
    create: { width: FG_CANVAS, height: FG_CANVAS, channels: 4, background: { r: 0, g: 0, b: 0, alpha: 0 } }
  })
    .composite([{ input: fgBuf, top: FG_OFFSET, left: FG_OFFSET }])
    .png()
    .toBuffer();

  // drawable/ic_foreground.png (Memozy_v2에도 반영)
  await sharp(foregroundBuf).toFile(path.join(drawableDir, 'ic_foreground.png'));
  fs.mkdirSync(path.join(MV2), { recursive: true });
  await sharp(foregroundBuf).toFile(path.join(MV2, 'ic_foreground.png'));
  console.log('  ✅ ic_foreground.png (576x576 캔버스, 아이콘 60% 크기)');

  // 4) mipmap PNG 생성
  console.log('\n📱 mipmap 아이콘 생성...');
  const mipmapDir = path.join(MV2, 'mipmap_png');
  fs.mkdirSync(mipmapDir, { recursive: true });

  for (const { dir, size } of MIPMAP_SIZES) {
    fs.mkdirSync(path.join(RES, dir), { recursive: true });
    const resized = await sharp(bufs.main).resize(size, size).png().toBuffer();
    await sharp(resized).toFile(path.join(RES, dir, 'ic_launcher.png'));
    // Memozy_v2/mipmap_png 에도
    const label = dir.replace('mipmap-', '');
    await sharp(resized).toFile(path.join(mipmapDir, `ic_launcher_${label}_${size}x${size}.png`));
    console.log(`  ✅ ${dir}/ic_launcher.png (${size}x${size})`);
  }

  // 5) playstore 512px
  await sharp(bufs.main)
    .resize(512, 512)
    .png()
    .toFile(path.join(ROOT, 'playstore_assets/ic_launcher_512.png'));
  await sharp(bufs.main)
    .resize(512, 512)
    .png()
    .toFile(path.join(ROOT, 'app/src/main/ic_launcher-playstore.png'));
  console.log('  ✅ playstore 512x512');

  // 6) ic_foreground_inset.xml (15% inset은 유지)
  const insetXml = `<?xml version="1.0" encoding="utf-8"?>
<inset xmlns:android="http://schemas.android.com/apk/res/android"
    android:drawable="@drawable/ic_foreground"
    android:inset="15%" />
`;
  fs.writeFileSync(path.join(drawableDir, 'ic_foreground_inset.xml'), insetXml);
  console.log('  ✅ ic_foreground_inset.xml');

  console.log('\n========================================');
  console.log('✅ 완료! Android Studio → Sync Project');
  console.log('========================================');
}

run().catch(e => { console.error('❌ 오류:', e.message); process.exit(1); });
