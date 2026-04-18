import me.pecos.memozy.convention.extension.setNamespace
import java.util.Properties

plugins {
    id("memozy.android.application")
    id("memozy.compose")
    id("memozy.hilt")
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

setNamespace("")

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

android {
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "me.pecos.memozy"
        versionCode = 3
        versionName = "1.2603.0"

        val admobAppId = localProperties.getProperty(
            "admob.app.id",
            "ca-app-pub-3940256099942544~3347511713"
        )
        manifestPlaceholders["admobAppId"] = admobAppId
    }
    buildTypes {
        debug {
            buildConfigField("String", "WORKER_URL", "\"${localProperties.getProperty("worker.url", "")}\"")
            buildConfigField("String", "APP_SECRET_KEY", "\"${localProperties.getProperty("app.secret.key", "")}\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "WORKER_URL", "\"${localProperties.getProperty("worker.prod.url", "")}\"")
            buildConfigField("String", "APP_SECRET_KEY", "\"${localProperties.getProperty("app.prod.secret.key", "")}\"")
        }
    }
}

dependencies {
    // 모듈 - Hilt 바인딩을 위해 implementation 필요
    implementation(projects.datasource.local.memo.impl)
    implementation(projects.datasource.local.memo.api) // 위젯에서 Memo 엔티티 참조
    implementation(projects.datasource.local.chat.api) // MemoDatabaseModule의 ChatSessionDao / ChatMessageDao provider
    implementation(projects.datasource.local.chat.impl)
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
    implementation(projects.datasource.remote.auth.api)
    implementation(projects.datasource.remote.auth.impl)
    implementation(projects.data.backup.api)
    implementation(projects.data.repository.memo.impl)
    implementation(projects.data.repository.memo.api) // 위젯에서 MemoRepository 참조
    implementation(projects.data.repository.chat.impl)
    implementation(projects.data.repository.chat.api) // ChatRepositoryModule의 ChatRepository 바인딩 참조
    implementation(projects.data.repository.user.impl)
    implementation(projects.data.backup.impl)
    implementation(projects.feature.core.resource)
    implementation(projects.feature.home.impl)
    implementation(projects.feature.memoPlain.impl)

    // core android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // date/time
    implementation(libs.kotlinx.datetime)

    // work manager
    implementation(libs.work.runtime)
    implementation(libs.work.hilt)
    ksp(libs.work.hilt.compiler)

    // glance widget
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)

    // ads
    implementation(libs.play.services.ads)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    // test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
