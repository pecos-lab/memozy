plugins {
    id("memozy.kmp.library")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.data.backup.impl"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.data.backup.api)
            implementation(projects.datasource.local.memo.api)
            implementation(projects.datasource.local.chat.api)
            implementation(projects.datasource.remote.auth.api)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.supabase.postgrest)
        }
    }
}
