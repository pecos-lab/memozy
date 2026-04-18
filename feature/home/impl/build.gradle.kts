import me.pecos.memozy.convention.extension.setNamespace
import java.util.Properties

plugins {
    id("memozy.android.library")
    id("memozy.compose")
}

setNamespace("feature.home.impl")

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

android {
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${localProperties.getProperty("google.web.client.id", "")}\"")
    }
}

dependencies {
    implementation(projects.feature.home.api)
    implementation(projects.feature.core.resource)
    implementation(projects.feature.core.viewmodel)
    implementation(projects.feature.memoPlain.api)
    implementation(projects.datasource.local.memo.api)
    implementation(projects.data.repository.memo.api)
    implementation(projects.data.repository.user.api)
    implementation(projects.datasource.remote.auth.api)
    implementation(projects.datasource.remote.ai.api)
    implementation(projects.data.backup.api)
    implementation(projects.platform.billing.api)
    implementation(projects.platform.ads.api)
    implementation(projects.platform.credential.api)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.haze)
    implementation(libs.kotlinx.datetime)
    implementation(libs.montage.android)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.koin.androidx.compose)
}
