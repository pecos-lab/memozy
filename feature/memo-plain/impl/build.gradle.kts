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
            implementation(libs.richeditor.compose)
            implementation(libs.compose.ui.backhandler)
            implementation(projects.feature.memoPlain.api)
            implementation(projects.feature.core.resource)
            implementation(projects.feature.core.viewmodel)
            implementation(projects.datasource.local.memo.api)
            implementation(projects.data.repository.memo.api)
            implementation(projects.datasource.remote.ai.api)
            implementation(projects.platform.media.api)
            implementation(projects.platform.intent.api)
            implementation(projects.platform.analytics.api)
            implementation(projects.platform.transcription.api)
            implementation(libs.haze)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(compose.materialIconsExtended)
            implementation(libs.kotlinx.datetime)
        }
        androidMain.configure {
            dependencies {
                implementation(libs.montage.android)
                implementation(libs.androidx.compose.navigation)
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.koin.androidx.compose)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.coil.compose)
                implementation(libs.androidx.compose.ui.tooling.preview)
            }
        }
    }
}
