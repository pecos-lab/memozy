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
            implementation(libs.google.fonts)
            api(projects.platform.ads.api)
            implementation(compose.materialIconsExtended)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "me.pecos.memozy.feature.core.resource.generated.resources"
}
