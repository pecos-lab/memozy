plugins {
    id("memozy.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.platform.credential.impl"
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.platform.credential.api)
        }
        androidMain.dependencies {
            implementation(libs.credential.manager)
            implementation(libs.credential.manager.play)
            implementation(libs.google.id.identity)
        }
    }
}
