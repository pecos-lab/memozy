<div align="center">

<img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white"/>
<img src="https://img.shields.io/badge/Min%20SDK-26-informational?style=flat-square"/>
<img src="https://img.shields.io/badge/Version-1.2603.0-blue?style=flat-square"/>
<img src="https://img.shields.io/github/license/pecos-lab/memozy?style=flat-square"/>

# 📝 Memozy

**YouTube 영상을 AI로 요약하고, 메모로 저장하는 Android 앱**

Gemini AI 기반 콘텐츠 요약 → 메모 자동 저장 파이프라인

</div>

---

## ✨ Key Features

| 기능 | 설명 |
|------|------|
| 🎬 YouTube 요약 | URL 붙여넣기 → Supadata API 자막 추출 → Gemini AI 요약 → 메모 자동 저장. 한국어 자막 우선, 영어 fallback |
| 🌐 웹페이지 요약 | URL 입력 → Cloudflare Workers 스크래핑 → Gemini AI 요약 |
| 🎙️ 음성 녹음 | 녹음 → 오디오 파일 저장 + 인라인 재생 |
| 📂 카테고리 분류 | 11가지 카테고리로 메모 분류, 카테고리별 필터링 |
| ✏️ 리치 텍스트 편집 | 굵기·색상·정렬 등 서식 툴바 |
| 🌙 테마 | 라이트 / 다크 / 시스템 모드 |
| 🌐 다국어 | 한국어 · English · 日本語 |
| ☁️ 클라우드 백업 | Google 로그인 → Supabase 클라우드 백업/복원 |
| 🔒 API 키 보안 | Supabase Edge Functions 프록시 패턴으로 클라이언트 API 키 노출 제로 |

---

## 🏗 Architecture

### 멀티모듈 구조

```
memozy/
├── app/                          # Application (Navigation, DI)
├── feature/
│   ├── core/resource/            # 공유 리소스 (strings, theme, components)
│   ├── home/{api,impl}/          # 홈·설정·로그인·휴지통 화면
│   └── memo-plain/{api,impl}/    # 메모 편집·요약·녹음 화면
├── data/
│   ├── repository/memo/{api,impl}/   # 메모 Repository
│   ├── repository/chat/{api,impl}/   # AI 채팅 Repository
│   ├── repository/user/{api,impl}/   # 유저 Repository
│   └── backup/{api,impl}/            # 백업 Repository
├── datasource/
│   ├── local/memo/{api,impl}/    # Room DB (메모)
│   ├── local/chat/{api,impl}/    # Room DB (채팅)
│   ├── remote/ai/{api,impl}/     # Gemini·Supadata API
│   └── remote/auth/{api,impl}/   # Supabase Auth
├── build-logic/                  # Convention Plugins
├── supabase/functions/           # Edge Functions (API 프록시)
├── workers/                      # Cloudflare Workers (웹 스크래핑·백업 API)
└── qa_test_case/                 # QA 테스트 케이스
```

### 아키텍처 패턴

```
UI (Compose) → ViewModel (MVVM) → Repository → DataSource (Room / Remote API)
```

- **Clean Architecture** — UI · Domain · Data 레이어 분리
- **MVVM + Repository Pattern** — ViewModel이 UI 상태 관리, Repository가 데이터 추상화
- **api/impl 모듈 분리** — 인터페이스와 구현을 모듈 레벨에서 격리

---

## 🛠 Tech Stack

**Language & UI**

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Material%203-757575?style=flat-square&logo=materialdesign&logoColor=white)

**Architecture & DI**

![MVVM](https://img.shields.io/badge/MVVM-Clean%20Architecture-orange?style=flat-square)
![Multi Module](https://img.shields.io/badge/Multi--Module-20+%20modules-green?style=flat-square)
![Hilt](https://img.shields.io/badge/Hilt-DI-FF6F00?style=flat-square&logo=google&logoColor=white)

**Data & Backend**

![Room](https://img.shields.io/badge/Room-Database-003B57?style=flat-square&logo=sqlite&logoColor=white)
![Supabase](https://img.shields.io/badge/Supabase-Edge%20Functions-3FCF8E?style=flat-square&logo=supabase&logoColor=white)
![Cloudflare](https://img.shields.io/badge/Cloudflare-Workers-F38020?style=flat-square&logo=cloudflare&logoColor=white)

**AI/ML**

![Gemini](https://img.shields.io/badge/Gemini-AI%20Summary-8E75B2?style=flat-square&logo=google&logoColor=white)
![Supadata](https://img.shields.io/badge/Supadata-Transcript%20API-333?style=flat-square)

**기타**

![Firebase](https://img.shields.io/badge/Firebase-Analytics%20%2B%20Crashlytics-FFCA28?style=flat-square&logo=firebase&logoColor=black)
![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-CI%2FCD-2088FF?style=flat-square&logo=githubactions&logoColor=white)
![Coroutines](https://img.shields.io/badge/Coroutines-Flow-7F52FF?style=flat-square&logo=kotlin&logoColor=white)

---

## 📊 Technical Highlights

| 항목 | 내용 |
|------|------|
| **Hilt DI 리팩터링** | MainActivity 960줄 → 217줄 (77% 감소) |
| **Room DB Migration** | 4회 스키마 마이그레이션, 데이터 무손실 |
| **멀티모듈** | 20+ 모듈, api/impl 분리로 빌드 격리 |
| **API 보안** | Supabase Edge Functions 프록시로 API 키 클라이언트 노출 제로 |
| **MemoScreen 리팩토링** | 1,697줄 → 컴포넌트 분리 (#104) |
| **373+ 커밋** | 기획부터 배포까지 전 과정 1인 개발 |

---

## 🎬 YouTube 요약 플로우

```
사용자: URL 입력
    │
    ▼
Supabase Edge Function (프록시)
    │
    ▼
Supadata API ── 자막 추출 (ko 우선 → en fallback)
    │
    ▼
Gemini API ── AI 요약 (스트리밍)
    │
    ▼
Room DB 저장 → UI 실시간 표시
```

---

## 📱 Screenshots

### ☀️ Light Mode

<div align="center">
<img src="screenshots/Screenshot_20260327_182153.png" width="18%"/>
<img src="screenshots/Screenshot_20260327_182202.png" width="18%"/>
<img src="screenshots/Screenshot_20260327_182222.png" width="18%"/>
</div>

### 🌙 Dark Mode

<div align="center">
<img src="screenshots/Screenshot_20260327_182303.png" width="18%"/>
<img src="screenshots/Screenshot_20260327_182323.png" width="18%"/>
<img src="screenshots/Screenshot_20260327_182326.png" width="18%"/>
</div>

---

## 🗺 Roadmap

- [x] YouTube 영상 요약 (Supadata + Gemini)
- [x] 웹페이지 URL 요약 (Cloudflare Workers)
- [x] 음성 녹음 + 오디오 재생
- [x] Hilt DI 전환
- [x] 멀티모듈 리팩터링 (20+ modules)
- [x] Room DB Migration (v1 → v5)
- [x] 클라우드 백업 (Supabase + Cloudflare Workers)
- [x] Google 로그인 (Supabase Auth)
- [x] Firebase Analytics / Crashlytics
- [x] 다국어 지원 (ko / en / ja)
- [x] 리치 텍스트 편집 (서식 툴바)
- [ ] 음성 녹음 디테일 개선 (#150)
- [ ] 인앱 결제 / 구독 (#149)
- [ ] 음악 인식 (ACRCloud) (#105)
- [ ] 웹페이지/뉴스 요약 고도화, 사진 OCR → 요약 (#63)

---

## 🚀 Getting Started

### 요구사항
- Android Studio Ladybug 이상
- Android 8.0 (API 26) 이상

### 빌드
```bash
git clone https://github.com/pecos-lab/memozy.git
cd memozy
./gradlew assembleDebug
```

---

## 📄 License

이 앱은 Apache License 2.0 하에 배포됩니다.

### Open Source Libraries

| 라이브러리 | 라이센스 |
|-----------|---------|
| [Wanted Design System (Montage)](https://montage.wanted.co.kr) | MIT License |
| [Jetpack Compose & AndroidX](https://developer.android.com/jetpack) | Apache License 2.0 |
| [Kotlin & kotlinx-coroutines](https://kotlinlang.org) | Apache License 2.0 |
| [Haze 1.7.2](https://github.com/chrisbanes/haze) | Apache License 2.0 |
| [Room](https://developer.android.com/jetpack/androidx/releases/room) | Apache License 2.0 |
| [Firebase (Crashlytics & Analytics)](https://firebase.google.com) | Apache License 2.0 |

---

<div align="center">

Made by [pecos-lab](https://github.com/pecos-lab)

</div>
