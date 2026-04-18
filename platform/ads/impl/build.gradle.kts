plugins {
    id("memozy.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.platform.ads.impl"
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.platform.ads.api)
        }
        androidMain.dependencies {
            implementation(libs.play.services.ads)
            implementation(libs.kotlinx.coroutines.android)
        }
    }
}
