import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

// `:shared:umbrella` ─ iOS framework `Shared` export용 umbrella 모듈.
// `:shared:poc`와 공존: poc는 Room KMP 스파이크(CI kmp-ios-check.yml 검증 대상),
// umbrella는 iosApp(Xcode)에 링크할 단일 framework 진입점. 역할이 겹치지 않음.
// Koin 바인딩/iOS 부트스트랩(sharedModule·initKoin·InMemoryMemoRepository)은
// 전부 `iosMain`에만 배치해 Android compileClasspath에 절대 유입되지 않도록 격리한다.
plugins {
    id("memozy.kmp.library")
    id("memozy.cmp.library")
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
        iosMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(projects.datasource.local.memo.impl)
            implementation(projects.datasource.remote.ai.api)
            implementation(projects.datasource.remote.ai.impl)
            implementation(projects.data.repository.memo.impl)
            implementation(projects.data.repository.user.api)
            implementation(projects.data.repository.user.impl)
            implementation(projects.data.backup.api)
            implementation(projects.feature.memoPlain.impl)
            implementation(projects.platform.intent.api)
            implementation(projects.platform.intent.impl)
            implementation(projects.platform.credential.api)
            implementation(projects.platform.credential.impl)
            implementation(projects.platform.ads.impl)
            implementation(projects.platform.media.api)
            implementation(projects.platform.media.impl)
        }
    }
}
