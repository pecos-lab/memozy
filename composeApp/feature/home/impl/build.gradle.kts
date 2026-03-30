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
    // Joda-Time 제거 완료 — java.time API 사용 (minSdk 26)
    implementation(libs.shadcn.compose)
    implementation(libs.billing.ktx)
    implementation(libs.koin.android)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.kotlinx.coroutines.android)
}
