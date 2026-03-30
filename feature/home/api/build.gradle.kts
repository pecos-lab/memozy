import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
    id("memozy.compose")
}

setNamespace("feature.home.api")

dependencies {
    implementation(libs.androidx.compose.navigation)
}
