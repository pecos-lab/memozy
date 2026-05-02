import java.util.Properties
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

plugins {
    id("memozy.kmp.library")
    id("memozy.cmp.library")
}

val androidNamespace = "me.pecos.memozy.feature.home.impl"

// KMP android library 플러그인(AGP 9.0)은 BuildConfig 생성을 지원하지 않으므로
// local.properties에서 읽은 Google Web Client ID를 androidMain 전용 Kotlin
// 소스로 생성한다. LoginScreen·SettingsScreen이 import하여 사용한다.
//
// 설계 원칙:
//   1) local.properties 파일은 ValueSource로 읽어 Gradle이 외부 입력으로
//      추적·직렬화한다. 스크립트 객체 캡처 없음, 파일 변경 시 CC 무효화.
//   2) 생성 로직은 abstract class task로 분리한다. @Input/@OutputDirectory
//      선언으로 Gradle이 incremental/Configuration Cache를 자동 관리하며
//      task action 내부에서 외부 람다 캡처가 일어나지 않는다.
//   3) 파일을 먼저 기록한 뒤 require()로 검증을 수행한다. require 실패 시
//      빈 GOOGLE_WEB_CLIENT_ID를 가진 파일이 남지만, 이는 local.properties
//      수정 시 @Input 해시가 변경되어 task가 재실행되므로 UP-TO-DATE 스킵
//      위험은 없다. 반대로 outputs.dir가 비어 있는 채로 실패하면 후속
//      compileAndroidMain이 이전 런의 stale 출력을 참조할 수 있어 방지.
//   4) clientId 값에 "/\가 포함되어도 Kotlin 문자열 리터럴이 깨지지 않도록
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

abstract class GenerateBuildConstantsTask : DefaultTask() {
    @get:Input
    abstract val clientId: Property<String>

    @get:Input
    abstract val packageName: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val clientIdValue = clientId.get()
        val packageNameValue = packageName.get()
        val escaped = clientIdValue
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
        val dir = outputDir.get().asFile.resolve(packageNameValue.replace('.', '/'))
        dir.mkdirs()
        dir.resolve("BuildConstants.kt").writeText(
            """|package $packageNameValue
               |
               |internal object BuildConstants {
               |    const val GOOGLE_WEB_CLIENT_ID: String = "$escaped"
               |}
               |""".trimMargin()
        )
        require(clientIdValue.isNotBlank()) {
            "google.web.client.id is missing in local.properties — Google 로그인이 " +
                "invalid_client 에러로 실패합니다. local.properties에 값을 추가하세요."
        }
    }
}

val googleWebClientIdProvider = providers.of(LocalPropertyValueSource::class) {
    parameters.propertiesFile.set(rootProject.layout.projectDirectory.file("local.properties"))
    parameters.key.set("google.web.client.id")
}

val generateBuildConstants = tasks.register<GenerateBuildConstantsTask>("generateBuildConstants") {
    clientId.set(googleWebClientIdProvider.orElse(""))
    packageName.set(androidNamespace)
    outputDir.set(layout.buildDirectory.dir("generated/source/buildConstants/androidMain/kotlin"))
}

kotlin {
    androidLibrary {
        namespace = androidNamespace
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
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
            implementation(projects.platform.analytics.api)
            implementation(projects.platform.credential.api)
            implementation(projects.platform.intent.api)
            implementation(libs.haze)
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin.compose)
            implementation(compose.materialIconsExtended)
        }
        androidMain.configure {
            // 명시적으로 task 출력 디렉토리를 source로 등록한다. Gradle이
            // task → compileAndroidMain 의존성을 자동 연결한다.
            kotlin.srcDir(generateBuildConstants.map { it.outputDir })
            dependencies {
                implementation(libs.androidx.lifecycle.runtime.ktx)
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.compose.navigation)
                implementation(libs.montage.android)
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.koin.androidx.compose)
                implementation(libs.androidx.compose.ui.tooling.preview)
            }
        }
    }
}
