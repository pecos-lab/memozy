plugins {
    id("memozy.kmp.library")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.datasource.remote.subscription.api"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.core.resource)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
