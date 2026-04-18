plugins {
    id("memozy.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.platform.ads.impl"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.platform.ads.api)
        }
    }
}
