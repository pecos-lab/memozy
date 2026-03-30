import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
    id("memozy.hilt")
}

setNamespace("data.repository.memo.impl")

dependencies {
    implementation(projects.composeApp.data.repository.memo.api)
    implementation(projects.composeApp.datasource.local.memo.api)
    implementation(libs.kotlinx.coroutines.android)
}
