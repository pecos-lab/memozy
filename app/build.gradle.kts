import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.application")
    id("memozy.compose")
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
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    // 모듈 - Hilt 바인딩을 위해 implementation 필요
    implementation(projects.datasource.local.memo.impl)
    implementation(projects.datasource.local.memo.api) // 위젯에서 Memo 엔티티 참조
    implementation(projects.datasource.local.chat.impl)
    implementation(projects.datasource.remote.ai.impl)
    implementation(projects.datasource.remote.auth.api)
    implementation(projects.datasource.remote.auth.impl)
    implementation(projects.data.backup.api)
    implementation(projects.data.repository.memo.impl)
    implementation(projects.data.repository.memo.api) // 위젯에서 MemoRepository 참조
    implementation(projects.data.repository.chat.impl)
    implementation(projects.data.repository.user.impl)
    implementation(projects.data.backup.impl)
    implementation(projects.feature.core.resource)
    implementation(projects.feature.home.impl)
    implementation(projects.feature.memoPlain.impl)

    // core android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // MemozyApplication
    implementation(libs.android.joda)

    // work manager
    implementation(libs.work.runtime)
    implementation(libs.work.hilt)
    ksp(libs.work.hilt.compiler)

    // glance widget
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    // test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
