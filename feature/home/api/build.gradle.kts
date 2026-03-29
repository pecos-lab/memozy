plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
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
        force(libs.androidx.compose.foundation.asProvider().get().toString())
        force(libs.androidx.compose.foundation.layout.get().toString())
    }
}

dependencies {
    implementation(libs.androidx.compose.navigation)
}
