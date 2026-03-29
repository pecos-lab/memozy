plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt.android)
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "me.pecos.memozy"
    compileSdk = 36

    defaultConfig {
        applicationId = "me.pecos.memozy"
        minSdk = 26
        targetSdk = 36
        versionCode = 3
        versionName = "1.2603.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

configurations.all {
    resolutionStrategy {
        force("androidx.compose.foundation:foundation:1.7.8")
        force("androidx.compose.foundation:foundation-layout:1.7.8")
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("dev.chrisbanes.haze:haze:1.7.2")
    implementation("net.danlew:android.joda:2.12.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation("com.github.wanteddev:montage-android:3.3.0")
    implementation("com.android.billingclient:billing-ktx:7.1.1")
    implementation("com.google.firebase:firebase-crashlytics")
    ksp(libs.room.compiler)
    implementation(libs.hilt.android)
    // hilt-navigation-compose 제거: OverrideNightMode의 LocalContext 오버라이드와 충돌
    // hiltViewModel()을 사용하지 않으므로 불필요
    // implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}