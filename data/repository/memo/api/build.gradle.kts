plugins {
    id("memozy.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.data.repository.memo.api"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.datasource.local.memo.api)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
