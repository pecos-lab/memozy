plugins {
    id("memozy.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.data.repository.user.api"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.datasource.remote.auth.api)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
