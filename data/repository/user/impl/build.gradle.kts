import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
    id("memozy.hilt")
}

setNamespace("data.repository.user.impl")

dependencies {
    implementation(projects.data.repository.user.api)
    implementation(projects.datasource.local.user.api)
    implementation(libs.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
}
