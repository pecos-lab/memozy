import me.pecos.memozy.convention.extension.configureAndroidLibrary

plugins {
    id("memozy.library")
}

configureAndroidLibrary("feature.memoplain.api")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.compose.navigation)
        }
    }
}
