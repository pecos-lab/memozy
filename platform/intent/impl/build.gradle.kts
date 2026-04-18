plugins {
    id("memozy.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.platform.intent.impl"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.platform.intent.api)
            implementation(libs.koin.core)
        }
        androidMain.configure {
            dependencies {
                implementation(libs.koin.android)
            }
        }
    }
}
