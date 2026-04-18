plugins {
    id("memozy.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.data.repository.user.impl"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.data.repository.user.api)
            implementation(projects.datasource.remote.auth.api)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
