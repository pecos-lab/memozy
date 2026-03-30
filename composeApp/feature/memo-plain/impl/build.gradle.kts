import me.pecos.memozy.convention.extension.configureAndroidLibrary

plugins {
    id("memozy.compose.library")
    id("memozy.koin")
}

configureAndroidLibrary("feature.memoplain.impl")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.composeApp.feature.memoPlain.api)
            implementation(projects.composeApp.feature.core.resource)
            implementation(projects.composeApp.datasource.local.memo.api)
            implementation(projects.composeApp.data.repository.memo.api)
            implementation(libs.shadcn.compose)
            implementation(libs.androidx.compose.navigation)
        }
    }
}
