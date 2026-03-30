import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
}

setNamespace("feature.memoplain.api")

dependencies {
    implementation(libs.androidx.compose.navigation)
}
