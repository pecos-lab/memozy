plugins {
    id("memozy.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.datasource.remote.auth.impl"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.datasource.remote.auth.api)
            implementation(libs.supabase.auth)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
