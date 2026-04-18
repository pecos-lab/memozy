plugins {
    id("memozy.kmp.library")
    id("memozy.cmp.library")
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.feature.core.resource"
        withHostTestBuilder { }
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.feature.core.viewmodel)
        }
        androidMain.dependencies {
            api(libs.google.fonts)
            implementation(compose.materialIconsExtended)
        }
    }
}
