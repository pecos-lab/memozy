plugins {
    id("memozy.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.data.repository.memo.impl"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.data.repository.memo.api)
            implementation(projects.datasource.local.memo.api)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }
    }
}
