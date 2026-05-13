import me.pecos.memozy.convention.extension.setNamespace
import java.util.Properties

plugins {
    id("memozy.android.application")
    id("memozy.compose")
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

setNamespace("")

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.bufferedReader(Charsets.UTF_8).use { load(it) }
}

// 비밀번호는 keystore.properties 의 secretsFile 이 가리키는 외부 파일(예: Google Drive)에서 로드한다.
// 로컬/리포지토리에 평문 비밀번호 사본을 남기지 않기 위함.
val keystoreSecrets = Properties().apply {
    val secretsPath = keystoreProperties.getProperty("secretsFile")
    if (secretsPath != null) {
        val file = rootProject.file(secretsPath)
        if (file.exists()) file.bufferedReader(Charsets.UTF_8).use { load(it) }
    }
}

android {
    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        create("release") {
            val storeFilePath = keystoreProperties.getProperty("storeFile")
            if (storeFilePath != null) {
                storeFile = file(storeFilePath)
                storePassword = keystoreSecrets.getProperty("storePassword")
                    ?: keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreSecrets.getProperty("keyPassword")
                    ?: keystoreProperties.getProperty("keyPassword")
                storeType = keystoreProperties.getProperty("storeType", "JKS")
            }
        }
    }

    defaultConfig {
        versionCode = 14
        versionName = "1.2605.9"

        val admobAppId = localProperties.getProperty(
            "admob.app.id",
            "ca-app-pub-3940256099942544~3347511713"
        )
        manifestPlaceholders["admobAppId"] = admobAppId
    }

    // 두 Play Console listing 동시 운영을 위한 flavor:
    //   memozy: 개발/내부 테스트용 (me.pecos.memozy 출시)
    //   nota:   기존 출시 listing 유지용 (me.pecos.nota — Play Store applicationId 영구 고정)
    // 빌드: ./gradlew :app:bundleMemozyRelease  /  ./gradlew :app:bundleNotaRelease
    flavorDimensions += "distribution"
    productFlavors {
        create("memozy") {
            dimension = "distribution"
            applicationId = "me.pecos.memozy"
        }
        create("nota") {
            dimension = "distribution"
            applicationId = "me.pecos.nota"
        }
    }
    buildTypes {
        // AdMob 광고 단위 ID — local.properties 미지정 시 AdMob 공식 테스트 ID 사용 (계정 정지 위험 제로).
        // 출시 빌드 전 admob.reward.ad.unit.id 를 실제 단위 ID 로 설정 + admob.test.device.ids 에 단말 ID 등록 필수.
        val testRewardAdUnitId = "ca-app-pub-3940256099942544/5224354917"
        debug {
            buildConfigField("String", "WORKER_URL", "\"${localProperties.getProperty("worker.url", "")}\"")
            buildConfigField("String", "APP_SECRET_KEY", "\"${localProperties.getProperty("app.secret.key", "")}\"")
            buildConfigField("String", "SUPABASE_URL", "\"${localProperties.getProperty("supabase.url", "")}\"")
            buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localProperties.getProperty("supabase.anon.key", "")}\"")
            buildConfigField("String", "ADMOB_REWARD_AD_UNIT_ID", "\"${localProperties.getProperty("admob.reward.ad.unit.id", testRewardAdUnitId)}\"")
            buildConfigField("String", "ADMOB_TEST_DEVICE_IDS", "\"${localProperties.getProperty("admob.test.device.ids", "")}\"")
            buildConfigField("String", "REVENUECAT_API_KEY", "\"${localProperties.getProperty("revenuecat.android.api.key", "")}\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "WORKER_URL", "\"${localProperties.getProperty("worker.prod.url", "")}\"")
            buildConfigField("String", "APP_SECRET_KEY", "\"${localProperties.getProperty("app.prod.secret.key", "")}\"")
            buildConfigField("String", "SUPABASE_URL", "\"${localProperties.getProperty("supabase.prod.url", "")}\"")
            buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localProperties.getProperty("supabase.prod.anon.key", "")}\"")
            buildConfigField("String", "ADMOB_REWARD_AD_UNIT_ID", "\"${localProperties.getProperty("admob.reward.ad.unit.id", testRewardAdUnitId)}\"")
            buildConfigField("String", "ADMOB_TEST_DEVICE_IDS", "\"${localProperties.getProperty("admob.test.device.ids", "")}\"")
            buildConfigField("String", "REVENUECAT_API_KEY", "\"${localProperties.getProperty("revenuecat.android.prod.api.key", "")}\"")
        }
    }
}

dependencies {
    // 모듈 - Hilt 바인딩을 위해 implementation 필요
    implementation(projects.datasource.local.memo.impl)
    implementation(projects.datasource.local.memo.api) // 위젯에서 Memo 엔티티 참조
    implementation(projects.datasource.local.chat.api) // MemoDatabaseModule의 ChatSessionDao / ChatMessageDao provider
    // Room: MemoDatabaseModule이 Builder/Migration/Callback API 직접 사용 (memo/impl이 KMP로 전환되며 implementation dep이 transitive로 노출 안 됨)
    implementation(libs.room.runtime)
    implementation(projects.datasource.remote.ai.impl)
    implementation(projects.datasource.remote.ai.api) // AiNetworkModule의 서비스 인터페이스 provider
    // Ktor: AiNetworkModule이 HttpClient/Json 직접 생성 (ai/impl이 KMP로 전환되며 implementation dep이 transitive로 노출 안 됨)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(projects.datasource.remote.auth.api) // AuthModule의 AuthService 바인딩 참조
    implementation(projects.datasource.remote.auth.impl)
    // Supabase: AuthModule이 SupabaseClient/Auth/Postgrest 직접 생성 (auth/impl이 KMP로 전환되며 implementation dep이 transitive로 노출 안 됨)
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(projects.data.backup.api)
    implementation(projects.data.repository.memo.impl)
    implementation(projects.data.repository.memo.api) // 위젯에서 MemoRepository 참조
    implementation(projects.data.repository.chat.impl)
    implementation(projects.data.repository.chat.api) // ChatRepositoryModule의 ChatRepository 바인딩 참조
    implementation(projects.data.repository.user.impl)
    implementation(projects.data.repository.user.api) // UserRepositoryModule의 AuthRepository 바인딩 참조
    implementation(projects.data.backup.impl)
    implementation(projects.feature.core.resource)
    implementation(projects.feature.core.viewmodel) // ViewModelModule의 MainViewModel/TrashViewModel/SettingsViewModel 바인딩 참조
    implementation(projects.feature.home.impl)
    implementation(projects.feature.memoPlain.impl)

    // platform (Android-only) impls wired through Koin modules
    implementation(projects.platform.billing.impl)
    implementation(projects.platform.ads.impl)
    implementation(projects.platform.analytics.impl)
    implementation(projects.platform.transcription.impl)
    implementation(projects.platform.credential.impl)
    implementation(projects.platform.media.api)
    implementation(projects.platform.media.impl)
    implementation(projects.platform.htmltext.api)
    implementation(projects.platform.htmltext.impl)
    implementation(projects.platform.intent.api)
    implementation(projects.platform.intent.impl)

    // core android
    implementation(libs.androidx.core.ktx)

    // date/time
    implementation(libs.kotlinx.datetime)

    // DI (Koin)
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.workmanager)

    // work manager
    implementation(libs.work.runtime)

    // glance widget
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    // test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
