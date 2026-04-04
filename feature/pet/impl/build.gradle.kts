import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
    id("memozy.compose")
    id("memozy.hilt")
}

setNamespace("feature.pet.impl")

dependencies {
    implementation(projects.feature.pet.api)
    implementation(projects.feature.core.resource)
    implementation(projects.datasource.local.pet.api)
    implementation(projects.datasource.local.memo.api)
    implementation(projects.data.repository.pet.api)
    implementation(projects.data.repository.memo.api)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
}
