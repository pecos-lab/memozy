plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "me.pecos.memozy.feature.home.impl"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
    buildFeatures { compose = true }
}

configurations.all {
    resolutionStrategy {
        force("androidx.compose.foundation:foundation:1.7.8")
        force("androidx.compose.foundation:foundation-layout:1.7.8")
    }
}

dependencies {
    api(project(":feature:home:api"))
    implementation(project(":feature:core:resource"))
    implementation(project(":feature:memo-plain:api"))
    implementation(project(":data:repository:memo:api"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("dev.chrisbanes.haze:haze:1.7.2")
    implementation("net.danlew:android.joda:2.12.7")
    implementation("com.github.wanteddev:montage-android:3.3.0")
    implementation("com.android.billingclient:billing-ktx:7.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
