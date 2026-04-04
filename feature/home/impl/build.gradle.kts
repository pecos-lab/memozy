import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
    id("memozy.compose")
    id("memozy.hilt")
}

setNamespace("feature.home.impl")

dependencies {
    implementation(projects.feature.home.api)
    implementation(projects.feature.core.resource)
    implementation(projects.feature.memoPlain.api)
    implementation(projects.feature.pet.api)
    implementation(projects.datasource.local.memo.api)
    implementation(projects.data.repository.memo.api)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.haze)
    implementation(libs.android.joda)
    implementation(libs.montage.android)
    implementation(libs.billing.ktx)
    implementation(libs.kotlinx.coroutines.android)
}
