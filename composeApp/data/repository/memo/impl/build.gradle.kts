import me.pecos.memozy.convention.extension.configureAndroidLibrary

plugins {
    id("memozy.library")
    id("memozy.koin")
}

configureAndroidLibrary("data.repository.memo.impl")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.composeApp.data.repository.memo.api)
            implementation(projects.composeApp.datasource.local.memo.api)
        }
    }
}
