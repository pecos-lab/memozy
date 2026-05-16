import org.gradle.api.file.DirectoryProperty
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import java.util.Properties

// `:shared:umbrella` ─ iOS framework `Shared` export용 umbrella 모듈.
// `:shared:poc`와 공존: poc는 Room KMP 스파이크(CI kmp-ios-check.yml 검증 대상),
// umbrella는 iosApp(Xcode)에 링크할 단일 framework 진입점. 역할이 겹치지 않음.
// Koin 바인딩/iOS 부트스트랩(sharedModule·initKoin·InMemoryMemoRepository)은
// 전부 `iosMain`에만 배치해 Android compileClasspath에 절대 유입되지 않도록 격리한다.
plugins {
    id("memozy.kmp.library")
    id("memozy.cmp.library")
}

// local.properties 의 secrets 키를 빌드 시점에 IosSecrets Kotlin object 로 주입.
// Android 의 `app/build.gradle.kts` BuildConfig 패턴과 대응 — 동일 local.properties 단일 출처.
// Info.plist + xcconfig 도 가능하나 dev 편의를 위해 자동 생성 방식 채택.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

val generateIosSecrets by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/secrets/iosMain/kotlin")
    outputs.dir(outputDir)
    val supabaseUrl = localProperties.getProperty("supabase.url", "")
    val supabaseAnonKey = localProperties.getProperty("supabase.anon.key", "")
    val workerUrl = localProperties.getProperty("worker.url", "")
    val appSecretKey = localProperties.getProperty("app.secret.key", "")
    val revenueCatApiKey = localProperties.getProperty("revenuecat.ios.api.key", "")
    inputs.property("supabaseUrl", supabaseUrl)
    inputs.property("supabaseAnonKey", supabaseAnonKey)
    inputs.property("workerUrl", workerUrl)
    inputs.property("appSecretKey", appSecretKey)
    inputs.property("revenueCatApiKey", revenueCatApiKey)
    doLast {
        val packageDir = outputDir.get().asFile.resolve("me/pecos/memozy/shared/umbrella")
        packageDir.mkdirs()
        packageDir.resolve("IosSecrets.kt").writeText(
            """
            package me.pecos.memozy.shared.umbrella

            /**
             * iOS secrets pipe — Android BuildConfig 와 대응.
             * 빌드 시점에 generateIosSecrets Gradle task 가 local.properties 값으로 자동 생성.
             * 누락된 키는 빈 문자열 → SupabaseClient init / AI 호출이 빈 URL 로 실패하게 됨.
             */
            internal object IosSecrets {
                const val supabaseUrl: String = "$supabaseUrl"
                const val supabaseAnonKey: String = "$supabaseAnonKey"
                const val workerUrl: String = "$workerUrl"
                const val appSecretKey: String = "$appSecretKey"
                const val revenueCatApiKey: String = "$revenueCatApiKey"
            }
            """.trimIndent()
        )
    }
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.shared.umbrella"
    }

    val xcf = XCFramework("Shared")

    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "Shared"
            isStatic = true
            xcf.add(this)
            export(projects.feature.core.viewmodel)
            export(projects.data.repository.memo.api)
            export(projects.platform.credential.impl)
            export(projects.platform.analytics.impl)
            export(projects.platform.transcription.impl)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.feature.core.viewmodel)
            api(projects.data.repository.memo.api)
            api(projects.feature.home.impl)
            implementation(projects.datasource.local.memo.api)
            implementation(projects.datasource.remote.auth.api)
            implementation(projects.feature.home.api)
            implementation(projects.feature.memoPlain.api)
            implementation(projects.feature.core.resource)
            implementation(projects.platform.ads.api)
            implementation(libs.jetbrains.navigation.compose)
            implementation(libs.haze)
            implementation(compose.materialIconsExtended)
        }
        iosMain {
            kotlin.srcDir(generateIosSecrets.map { it.outputs.files })
        }
        iosMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(projects.datasource.local.memo.impl)
            implementation(projects.datasource.local.chat.api)
            implementation(projects.datasource.remote.ai.api)
            implementation(projects.datasource.remote.ai.impl)
            implementation(projects.datasource.remote.auth.impl)
            implementation(projects.data.repository.memo.impl)
            implementation(projects.data.repository.user.api)
            implementation(projects.data.repository.user.impl)
            implementation(projects.data.backup.api)
            implementation(projects.data.backup.impl)
            implementation(projects.feature.memoPlain.impl)
            implementation(projects.platform.intent.api)
            implementation(projects.platform.intent.impl)
            implementation(projects.platform.credential.api)
            api(projects.platform.credential.impl)
            implementation(projects.platform.ads.impl)
            api(projects.platform.analytics.impl)
            api(projects.platform.transcription.impl)
            implementation(projects.platform.billing.api)
            implementation(projects.platform.billing.impl)
            implementation(projects.platform.media.api)
            implementation(projects.platform.media.impl)
            // Supabase — iosMain SupabaseClient 직접 생성용. KMP commonMain 에서는 platform(BOM)
            // deprecated 되어 개별 lib 의 version.ref 만으로 처리.
            implementation(libs.supabase.auth)
            implementation(libs.supabase.postgrest)
        }
    }
}

// CMP 1.10.3 + Gradle 9.3.1 호환 — SyncComposeResourcesForIosTask 의 outputDir 가
// Xcode env 미감지 시 wiring 안 되어 Gradle strict validation 에 걸리는 회귀 우회.
// 클래스가 internal 이라 reflection 으로 outputDir 프로퍼티 접근 + default 주입.
// Xcode 빌드 시에는 plugin 이 실제 BUILT_PRODUCTS_DIR 로 덮어쓰므로 default 만 제공.
//
// TODO(#363): 임시 workaround. 다음 조건 충족 시 블록 전체 제거:
//   1) CMP > 1.10.3 (outputDir 가 default value 를 갖도록 plugin 수정 시), 또는
//   2) Gradle < 9 다운그레이드 (strict property validation 미적용).
// 업그레이드 시: 본 블록 삭제 후 ./gradlew :shared:umbrella:embedAndSignAppleFrameworkForXcode
// 가 outputDir 에러 없이 통과하는지 검증.
afterEvaluate {
    // prefix 매칭 — 단일 syncComposeResourcesForIos 외에 syncComposeResourcesForIosArm64 /
    // syncComposeResourcesForIosX64 / syncComposeResourcesForIosSimulatorArm64 등 per-target
    // variant 가 있어도 모두 처리.
    val matched = tasks.filter { it.name.startsWith("syncComposeResourcesForIos") }
    if (matched.isEmpty()) {
        logger.warn("syncComposeResourcesForIos* task not found in afterEvaluate")
    }
    matched.forEach { task ->
        try {
            val getter = task::class.java.methods
                .firstOrNull { it.name == "getOutputDir" && it.parameterCount == 0 }
            if (getter == null) {
                logger.warn("CMP getOutputDir 메서드 누락 — CMP API 변경 가능, ${task.name} skip")
                return@forEach
            }
            val outputDirProp = getter.invoke(task) as? DirectoryProperty
            if (outputDirProp == null) {
                logger.warn("CMP outputDir 반환 타입 mismatch — CMP API 변경 가능, ${task.name} skip")
                return@forEach
            }
            outputDirProp.set(
                layout.buildDirectory.dir("compose/cmp-ios-resources/${task.name}")
            )
            logger.lifecycle("CMP outputDir wired for ${task.name} → ${outputDirProp.orNull?.asFile?.path}")
        } catch (e: Throwable) {
            logger.warn("Failed to set outputDir on ${task.name}: ${e.message}")
        }
    }
}
