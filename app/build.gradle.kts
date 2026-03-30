plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt.android)
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "me.pecos.memozy"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "me.pecos.memozy"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.compileSdk.get().toInt()
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
}

dependencies {
    // 모듈 - Hilt가 런타임에 바인딩 발견
    runtimeOnly(projects.datasource.local.memo.impl)
    runtimeOnly(projects.data.repository.memo.impl)
    runtimeOnly(projects.feature.home.impl)
    runtimeOnly(projects.feature.memoPlain.impl)

    // core android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // MemozyApplication
    implementation(libs.android.joda)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    // test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
