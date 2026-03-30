import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
    id("memozy.hilt")
}

setNamespace("data.repository.memo.impl")

dependencies {
    implementation(projects.data.repository.memo.api)
    implementation(projects.datasource.local.memo.api)
    implementation(libs.kotlinx.coroutines.android)
}
