import java.util.Properties
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

plugins {
    id("memozy.kmp.library")
    id("memozy.cmp.library")
}

// KMP android library 플러그인(AGP 9.0)은 BuildConfig 생성을 지원하지 않으므로
// local.properties에서 읽은 Google Web Client ID를 androidMain 전용 Kotlin
// 소스로 생성한다. LoginScreen·SettingsScreen이 import하여 사용한다.
//
// Configuration Cache 호환을 위해 다음 원칙을 따른다:
//   1) local.properties 파일은 ValueSource로 읽어 Gradle이 외부 입력으로
//      추적·직렬화한다. (스크립트 객체 캡처 없음, 파일 변경 시 CC 무효화.)
//   2) require() 검증은 task action(doLast) 안에서만 실행한다. 다른 모듈의
//      무관한 태스크 실행 시 Configuration 단계에서 빌드가 실패하지 않는다.
//   3) clientId 값에 "/\가 포함되어도 Kotlin 문자열 리터럴이 깨지지 않도록
//      이스케이프 후 파일에 기록한다.
abstract class LocalPropertyValueSource : ValueSource<String, LocalPropertyValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        val propertiesFile: RegularFileProperty
        val key: Property<String>
    }

    override fun obtain(): String? {
        val file = parameters.propertiesFile.asFile.orNull ?: return null
        if (!file.exists()) return null
        return Properties().apply {
            file.inputStream().use { load(it) }
        }.getProperty(parameters.key.get())
    }
}

val googleWebClientIdProvider = providers.of(LocalPropertyValueSource::class) {
    parameters.propertiesFile.set(rootProject.layout.projectDirectory.file("local.properties"))
    parameters.key.set("google.web.client.id")
}

val generateBuildConstants by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/source/buildConstants/androidMain/kotlin")
    val clientIdInput = googleWebClientIdProvider
    outputs.dir(outputDir)
    inputs.property("clientId", clientIdInput.orElse(""))
    doLast {
        val clientId = clientIdInput.orNull.orEmpty()
        require(clientId.isNotBlank()) {
            "google.web.client.id is missing in local.properties — Google 로그인이 " +
                "invalid_client 에러로 실패합니다. local.properties에 값을 추가하세요."
        }
        val escaped = clientId
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
        val dir = outputDir.get().asFile.resolve("me/pecos/memozy/feature/home/impl")
        dir.mkdirs()
        dir.resolve("BuildConstants.kt").writeText(
            """
            |package me.pecos.memozy.feature.home.impl
            |
            |internal object BuildConstants {
            |    const val GOOGLE_WEB_CLIENT_ID: String = "$escaped"
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
            dependencies {
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
}
