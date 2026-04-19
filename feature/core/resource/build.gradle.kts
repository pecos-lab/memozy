plugins {
    id("memozy.kmp.library")
    id("memozy.cmp.library")
    alias(libs.plugins.kotlin.serialization)
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
            api(projects.platform.ads.api)
            api(projects.platform.intent.api)
            implementation(compose.materialIconsExtended)
            implementation(libs.kotlinx.serialization.json)
        }
        androidMain.dependencies {
            implementation(libs.google.fonts)
            implementation(libs.coil.compose)
            implementation(libs.androidx.activity.compose)
            implementation(libs.montage.android)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "me.pecos.memozy.feature.core.resource.generated.resources"
}
