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

    defaultConfig {
        buildConfigField("String", "AI_API_KEY", "\"${localProperties.getProperty("ai.api.key", "")}\"")
        buildConfigField("String", "AI_BASE_URL", "\"${localProperties.getProperty("ai.base.url", "https://generativelanguage.googleapis.com/v1beta/")}\"")
        buildConfigField("String", "AI_MODEL", "\"${localProperties.getProperty("ai.model", "gemini-2.5-flash")}\"")
        buildConfigField("String", "SUPADATA_API_KEY", "\"${localProperties.getProperty("supadata.api.key", "")}\"")
    }
}

dependencies {
    implementation(projects.datasource.remote.ai.api)
}
