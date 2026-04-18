plugins {
    id("memozy.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.platform.credential.impl"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.platform.credential.api)
        }
    }
}
