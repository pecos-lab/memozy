plugins {
    id("memozy.kmp.library")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.feature.core.viewmodel"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.datasource.local.memo.api)
            implementation(projects.data.repository.memo.api)
            implementation(projects.data.backup.api)
            implementation(projects.data.repository.user.api)
            implementation(projects.datasource.remote.auth.api)
            implementation(projects.platform.analytics.api)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.androidx.lifecycle.viewmodel)
        }
    }
}
