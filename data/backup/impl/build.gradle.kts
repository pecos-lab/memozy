import me.pecos.memozy.convention.extension.setNamespace
import java.util.Properties

plugins {
    id("memozy.android.library")
    id("memozy.hilt")
    id("memozy.ktor")
}

setNamespace("data.backup.impl")

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

android {
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "WORKER_URL", "\"${localProperties.getProperty("worker.url", "")}\"")
    }
}

dependencies {
    implementation(projects.data.backup.api)
    implementation(projects.datasource.local.memo.api)
    implementation(projects.datasource.local.chat.api)
    implementation(projects.datasource.remote.auth.api)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.room.ktx)
}
