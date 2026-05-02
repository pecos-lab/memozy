plugins {
    id("memozy.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.platform.transcription.api"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
