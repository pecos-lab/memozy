# Add project specific ProGuard rules here.
# 참고: 라이브러리별 공식 권장 룰 + 프로젝트 keep 룰.

# ============================================================
# 디버깅을 위한 라인 정보 보존 (Crashlytics deobfuscation 정확도)
# ============================================================
-keepattributes SourceFile,LineNumberTable,*Annotation*,InnerClasses,EnclosingMethod,Signature,Exceptions
-renamesourcefileattribute SourceFile

# ============================================================
# Kotlin 메타데이터
# ============================================================
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$Companion { *; }
-keepclassmembers class kotlin.coroutines.jvm.internal.** { *; }

# ============================================================
# kotlinx.serialization — @Serializable 클래스의 $$serializer 보존
# ============================================================
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keep,includedescriptorclasses class **$$serializer { *; }
-keepclassmembers class ** {
    *** Companion;
}
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}
# 프로젝트 모델: @Serializable 클래스의 자기 직렬화 메서드 keep
-keepclassmembers @kotlinx.serialization.Serializable class * {
    static <fields>;
    static **$Companion Companion;
    static kotlinx.serialization.KSerializer serializer(...);
}

# ============================================================
# Koin DI — 리플렉션으로 모듈/스코프 검색
# ============================================================
-keep class org.koin.** { *; }
-keep class org.koin.core.** { *; }
-keep class org.koin.android.** { *; }
-keep class org.koin.androidx.** { *; }
-dontwarn org.koin.**
# 프로젝트 모듈 정의 함수
-keepclassmembers class me.pecos.** {
    public static org.koin.core.module.Module *();
}

# ============================================================
# Ktor — 리플렉션으로 ContentNegotiation/Serialization 등 검색
# ============================================================
-keep class io.ktor.** { *; }
-keep class io.ktor.client.** { *; }
-keep class io.ktor.serialization.** { *; }
-dontwarn io.ktor.**
-dontwarn org.slf4j.**

# ============================================================
# Supabase — kotlinx.serialization 사용 + 내부 리플렉션
# ============================================================
-keep class io.github.jan.supabase.** { *; }
-keep interface io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**

# ============================================================
# RevenueCat purchases-kmp + Hybrid Common
# ============================================================
-keep class com.revenuecat.purchases.** { *; }
-keep interface com.revenuecat.purchases.** { *; }
-dontwarn com.revenuecat.purchases.**

# ============================================================
# Firebase / Crashlytics / Analytics
# ============================================================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ============================================================
# Google Sign-In / Credential Manager
# ============================================================
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.** { *; }
-dontwarn androidx.credentials.**

# ============================================================
# Room — KSP 코드 생성이 대부분 처리하지만 방어용
# ============================================================
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.TypeConverter class * { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# ============================================================
# SQLite Bundled Driver — JNI_OnLoad 에서 native 메서드를 정확한 클래스명/시그니처로
# 찾아 등록하는데, R8 가 클래스명 난독화하면 등록 실패 → UnsatisfiedLinkError → 크래시.
# 따라서 androidx.sqlite.driver.bundled.* 의 클래스 이름·native 메서드를 보존해야 함.
# (실제 발견 사례: androidx.sqlite.driver.bundled.BundledSQLiteDriverKt.nativeThreadSafeMode)
# ============================================================
-keep class androidx.sqlite.** { *; }
-keep class androidx.sqlite.driver.bundled.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}

# ============================================================
# AndroidX Glance (위젯)
# ============================================================
-keep class androidx.glance.** { *; }
-keep class androidx.compose.ui.text.** { *; }
-dontwarn androidx.glance.**
-keepclassmembers class * extends androidx.glance.appwidget.GlanceAppWidget { *; }
-keepclassmembers class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }

# ============================================================
# WorkManager
# ============================================================
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keepclassmembers class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ============================================================
# Compose / Material — 보통 안 깨지지만 방어
# ============================================================
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ============================================================
# Coroutines — 디버깅 정보 keep
# ============================================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# ============================================================
# Joda Time (kotlinx-datetime로 마이그레이션 진행 중이지만 잔재 가능)
# ============================================================
-dontwarn org.joda.time.**

# ============================================================
# 프로젝트 도메인 모델 — @Serializable / @Parcelize / Room Entity 사용 클래스
# 보수적으로 me.pecos.** 의 직렬화 관련 멤버 모두 keep
# ============================================================
-keep class me.pecos.**$$serializer { *; }
-keepclassmembers class me.pecos.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep @kotlinx.serialization.Serializable class me.pecos.** { *; }

# ============================================================
# OkHttp / Retrofit (Ktor가 내부적으로 OkHttp 엔진 사용)
# ============================================================
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
