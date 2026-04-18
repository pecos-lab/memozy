plugins {
    id("memozy.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.data.repository.chat.impl"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.data.repository.chat.api)
            implementation(projects.datasource.local.chat.api)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }
    }
}
