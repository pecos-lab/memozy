import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
}

setNamespace("data.repository.memo.api")

dependencies {
    implementation(projects.composeApp.datasource.local.memo.api)
    implementation(libs.kotlinx.coroutines.android)
}
