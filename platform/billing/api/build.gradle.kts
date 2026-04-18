plugins {
    id("memozy.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.platform.billing.api"
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.feature.core.resource)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
