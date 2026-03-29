plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "me.pecos.memozy.feature.core.resource"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
}
