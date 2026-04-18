plugins {
    id("memozy.kmp.library")
    id("memozy.cmp.library")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.feature.memoplain.impl"
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
        // 순수 Composable commonMain 이전은 후속 PR에서 R.*→compose-resources
        // 마이그레이션과 함께 진행 (Issue #231 Wave 2 follow-up).
        androidMain.configure {
            dependencies {
                implementation(projects.feature.memoPlain.api)
                implementation(projects.feature.core.resource)
                implementation(projects.feature.core.viewmodel)
                implementation(projects.datasource.local.memo.api)
                implementation(projects.data.repository.memo.api)
                implementation(projects.datasource.remote.ai.api)
                implementation(projects.platform.media.api)
                implementation(libs.montage.android)
                implementation(libs.androidx.compose.navigation)
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.koin.core)
                implementation(libs.koin.androidx.compose)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(compose.materialIconsExtended)
                implementation(libs.richeditor.compose)
                implementation(libs.coil.compose)
                implementation(libs.haze)
                implementation(libs.androidx.compose.ui.tooling.preview)
            }
        }
    }
}
