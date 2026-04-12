import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
    alias(libs.plugins.kotlin.serialization)
}

setNamespace("data.backup.api")

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
}
