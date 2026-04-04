import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
    id("memozy.compose")
    id("memozy.hilt")
}

setNamespace("feature.memoplain.impl")

dependencies {
    implementation(projects.feature.memoPlain.api)
    implementation(projects.feature.core.resource)
    implementation(projects.datasource.local.memo.api)
    implementation(projects.data.repository.memo.api)
    implementation(projects.data.repository.pet.api)
    implementation(projects.datasource.local.pet.api)
    implementation(libs.montage.android)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.kotlinx.coroutines.android)
}
