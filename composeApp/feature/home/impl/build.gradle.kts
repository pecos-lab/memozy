import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
    id("memozy.compose")
}

setNamespace("feature.home.impl")

dependencies {
    implementation(projects.composeApp.feature.home.api)
    implementation(projects.composeApp.feature.core.resource)
    implementation(projects.composeApp.feature.memoPlain.api)
    implementation(projects.composeApp.datasource.local.memo.api)
    implementation(projects.composeApp.data.repository.memo.api)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.haze)
    implementation(libs.android.joda)
    implementation(libs.montage.android)
    implementation(libs.billing.ktx)
    implementation(libs.koin.android)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.kotlinx.coroutines.android)
}
