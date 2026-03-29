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
    buildFeatures {
        compose = true
    }
}

configurations.all {
    resolutionStrategy {
        force("androidx.compose.foundation:foundation:${libs.versions.composeFoundation.get()}")
        force("androidx.compose.foundation:foundation-layout:${libs.versions.composeFoundation.get()}")
    }
}

dependencies {
    // 모듈
    implementation(project(":datasource:local:memo:impl"))
    implementation(project(":data:repository:memo:impl"))
    implementation(project(":feature:core:resource"))
    implementation(project(":feature:home:impl"))
    implementation(project(":feature:memo-plain:impl"))

    // core android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)

    // compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)

    // navigation
    implementation(libs.androidx.compose.navigation)

    // haze (MainActivity 네비게이션 바 glass 효과)
    implementation(libs.haze)

    // hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // app이 직접 사용하는 라이브러리 (feature:home:impl에서 implementation으로 선언되어 전이되지 않음)
    implementation(libs.montage.android)   // MainActivity, BottomNavBar
    implementation(libs.billing.ktx)  // BillingManager 상위 타입
    implementation(libs.android.joda)               // MemozyApplication

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    // test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
