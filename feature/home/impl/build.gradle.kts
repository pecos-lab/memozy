import java.util.Properties

plugins {
    id("memozy.kmp.library")
    id("memozy.cmp.library")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

// KMP android library 플러그인(AGP 9.0)은 BuildConfig 생성을 지원하지 않으므로
// local.properties에서 읽은 Google Web Client ID를 androidMain 전용 Kotlin
// 소스로 생성한다. LoginScreen·SettingsScreen이 import하여 사용한다.
//
// Configuration Cache 호환을 위해 doLast 람다는 outer Project 상태를 직접
// 캡처하지 않는다. clientId는 providers.provider로 lazy 평가하고,
// task 내부에서는 inputs.properties["clientId"]를 통해 읽는다.
val clientIdProvider = providers.provider {
    val value = localProperties.getProperty("google.web.client.id", "")
    require(value.isNotBlank()) {
        "google.web.client.id is missing in local.properties — Google 로그인이 " +
            "invalid_client 에러로 실패합니다. local.properties에 값을 추가하세요."
    }
    value
}

val generateBuildConstants by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/source/buildConstants/androidMain/kotlin")
    outputs.dir(outputDir)
    inputs.property("clientId", clientIdProvider)
    doLast {
        val clientId = inputs.properties["clientId"] as String
        val dir = outputDir.get().asFile.resolve("me/pecos/memozy/feature/home/impl")
        dir.mkdirs()
        dir.resolve("BuildConstants.kt").writeText(
            """
            |package me.pecos.memozy.feature.home.impl
            |
            |internal object BuildConstants {
            |    const val GOOGLE_WEB_CLIENT_ID: String = "$clientId"
            |}
            """.trimMargin()
        )
    }
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.feature.home.impl"
        androidResources {
            enable = true
        }
    }

    sourceSets {
        // commonMain은 현재 비어 있음.
        // 순수 Composable commonMain 이전은 후속 PR에서 R.*→compose-resources
        // 마이그레이션과 함께 진행 (Issue #231 Wave 2 follow-up).
        androidMain.configure {
            kotlin.srcDir(generateBuildConstants)
        }
        androidMain.dependencies {
            implementation(projects.feature.home.api)
            implementation(projects.feature.core.resource)
            implementation(projects.feature.core.viewmodel)
            implementation(projects.feature.memoPlain.api)
            implementation(projects.datasource.local.memo.api)
            implementation(projects.data.repository.memo.api)
            implementation(projects.data.repository.user.api)
            implementation(projects.datasource.remote.auth.api)
            implementation(projects.datasource.remote.ai.api)
            implementation(projects.data.backup.api)
            implementation(projects.platform.billing.api)
            implementation(projects.platform.ads.api)
            implementation(projects.platform.credential.api)
            implementation(libs.androidx.lifecycle.runtime.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.compose.navigation)
            implementation(libs.haze)
            implementation(libs.kotlinx.datetime)
            implementation(libs.montage.android)
            implementation(libs.kotlinx.coroutines.android)
            implementation(compose.materialIconsExtended)
            implementation(libs.koin.androidx.compose)
        }
    }
}
