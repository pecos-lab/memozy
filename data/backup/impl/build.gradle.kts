import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
    id("memozy.hilt")
    alias(libs.plugins.kotlin.serialization)
}

setNamespace("data.backup.impl")

dependencies {
    implementation(projects.data.backup.api)
    implementation(projects.datasource.local.memo.api)
    implementation(projects.datasource.local.chat.api)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.room.ktx)
}
