import me.pecos.memozy.convention.extension.setNamespace
import java.util.Properties

plugins {
    id("memozy.android.library")
    id("memozy.hilt")
    id("memozy.ktor")
}

setNamespace("datasource.remote.ai.impl")

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

android {
    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField("String", "WORKER_URL", "\"${localProperties.getProperty("worker.url", "")}\"")
            buildConfigField("String", "APP_SECRET_KEY", "\"${localProperties.getProperty("app.secret.key", "")}\"")
        }
        release {
            buildConfigField("String", "WORKER_URL", "\"${localProperties.getProperty("worker.prod.url", "")}\"")
            buildConfigField("String", "APP_SECRET_KEY", "\"${localProperties.getProperty("app.prod.secret.key", "")}\"")
        }
    }
}

dependencies {
    implementation(projects.datasource.remote.ai.api)
}
