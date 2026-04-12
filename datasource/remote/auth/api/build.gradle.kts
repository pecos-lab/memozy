import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
}

setNamespace("datasource.remote.auth.api")

dependencies {
    implementation(libs.kotlinx.coroutines.android)
}
