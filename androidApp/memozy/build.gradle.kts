import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.application")
    id("memozy.hilt")
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

setNamespace("")

android {
    defaultConfig {
        applicationId = "me.pecos.memozy"
        versionCode = 3
        versionName = "1.2603.0"
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
}

dependencies {
    // 모듈 - Hilt 바인딩을 위해 implementation 필요
    implementation(projects.composeApp.datasource.local.memo.impl)
    implementation(projects.composeApp.data.repository.memo.impl)
    implementation(projects.composeApp.feature.home.impl)
    implementation(projects.composeApp.feature.memoPlain.impl)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.core)

    // core android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

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
