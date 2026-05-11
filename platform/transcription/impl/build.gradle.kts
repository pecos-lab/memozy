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
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.websockets)
            implementation(libs.kotlinx.serialization.json)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.ktor.client.okhttp)
            // RecordingService 의 PCM listener / state 노출에 의존
            implementation(projects.platform.media.impl)
        }
    }
}
