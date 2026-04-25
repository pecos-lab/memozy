plugins {
    id("memozy.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.data.repository.subscription.api"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.core.resource)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
