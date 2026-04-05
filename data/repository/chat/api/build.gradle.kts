import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
}

setNamespace("data.repository.chat.api")

dependencies {
    implementation(projects.datasource.local.chat.api)
    implementation(libs.kotlinx.coroutines.android)
}
