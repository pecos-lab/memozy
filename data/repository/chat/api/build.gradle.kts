plugins {
    id("memozy.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.data.repository.chat.api"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.datasource.local.chat.api)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
