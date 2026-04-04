import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
}

setNamespace("data.repository.pet.api")

dependencies {
    implementation(projects.datasource.local.pet.api)
    implementation(libs.kotlinx.coroutines.android)
}
