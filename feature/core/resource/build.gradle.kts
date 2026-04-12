import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
    id("memozy.compose")
}

setNamespace("feature.core.resource")

dependencies {
    api(libs.google.fonts)
}
