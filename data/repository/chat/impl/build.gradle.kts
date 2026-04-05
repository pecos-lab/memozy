import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
    id("memozy.hilt")
}

setNamespace("data.repository.chat.impl")

dependencies {
    implementation(projects.data.repository.chat.api)
    implementation(projects.datasource.local.chat.api)
    implementation(libs.kotlinx.coroutines.android)
}
