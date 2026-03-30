import me.pecos.memozy.convention.extension.configureAndroidLibrary

plugins {
    id("memozy.library")
}

configureAndroidLibrary("feature.home.api")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.compose.navigation)
        }
    }
}
