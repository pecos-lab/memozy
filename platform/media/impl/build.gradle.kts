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
    }
}
