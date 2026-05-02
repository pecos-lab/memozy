plugins {
    id("memozy.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.platform.media.impl"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.platform.media.api)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
