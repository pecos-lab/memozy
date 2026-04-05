import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
}

setNamespace("data.repository.user.api")

dependencies {
    implementation(projects.datasource.local.user.api)
    implementation(libs.kotlinx.coroutines.android)
}
