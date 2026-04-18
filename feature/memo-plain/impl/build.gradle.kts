import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
    id("memozy.compose")
}

setNamespace("feature.memoplain.impl")

dependencies {
    implementation(projects.feature.memoPlain.api)
    implementation(projects.feature.core.resource)
    implementation(projects.feature.core.viewmodel)
    implementation(projects.datasource.local.memo.api)
    implementation(projects.data.repository.memo.api)
    implementation(projects.datasource.remote.ai.api)
    implementation(projects.platform.media.api)
    implementation(libs.montage.android)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.koin.core)
    implementation(libs.koin.androidx.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.richeditor.compose)
    implementation(libs.coil.compose)
    implementation(libs.haze)
}
