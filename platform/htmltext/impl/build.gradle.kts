plugins {
    id("memozy.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.platform.htmltext.impl"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.platform.htmltext.api)
        }
    }
}
