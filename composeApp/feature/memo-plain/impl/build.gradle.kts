import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
    id("memozy.compose")
    id("memozy.hilt")
}

setNamespace("feature.memoplain.impl")

dependencies {
    implementation(projects.composeApp.feature.memoPlain.api)
    implementation(projects.composeApp.feature.core.resource)
    implementation(projects.composeApp.datasource.local.memo.api)
    implementation(projects.composeApp.data.repository.memo.api)
    implementation(libs.koin.core)
    implementation(libs.montage.android)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.kotlinx.coroutines.android)
}
