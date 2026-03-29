plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "me.pecos.memozy.feature.home.api"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
    buildFeatures { compose = true }
}

configurations.all {
    resolutionStrategy {
        force("androidx.compose.foundation:foundation:${libs.versions.composeFoundation.get()}")
        force("androidx.compose.foundation:foundation-layout:${libs.versions.composeFoundation.get()}")
    }
}

dependencies {
    api(libs.androidx.compose.navigation)
}
