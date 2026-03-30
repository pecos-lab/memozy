import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
}

setNamespace("feature.memo_plain.api")

dependencies {
    implementation(libs.androidx.compose.navigation)
}
