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
val generateBuildConstants by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/source/buildConstants/androidMain/kotlin")
    val clientId = localProperties.getProperty("google.web.client.id", "")
    outputs.dir(outputDir)
    inputs.property("clientId", clientId)
    doLast {
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
        withHostTestBuilder { }
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // 순수 Composable commonMain 이전은 후속 PR에서 점진적으로 진행.
            // 현재 이 모듈의 UI는 모두 androidMain 유지 (BuildConstants·R.*·Android
            // 위젯에 의존).
        }
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

