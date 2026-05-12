plugins {
    id("memozy.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.platform.transcription.impl"
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.platform.transcription.api)
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
        }
    }
}
