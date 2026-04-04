import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
    id("memozy.hilt")
}

setNamespace("data.repository.pet.impl")

dependencies {
    implementation(projects.data.repository.pet.api)
    implementation(projects.datasource.local.pet.api)
    implementation(projects.datasource.local.memo.api)
    implementation(libs.kotlinx.coroutines.android)
}
