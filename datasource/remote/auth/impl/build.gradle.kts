import me.pecos.memozy.convention.extension.setNamespace
import java.util.Properties

plugins {
    id("memozy.android.library")
    id("memozy.hilt")
    id("memozy.ktor")
}

setNamespace("datasource.remote.auth.impl")

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

android {
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "SUPABASE_URL", "\"${localProperties.getProperty("supabase.url", "")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localProperties.getProperty("supabase.anon.key", "")}\"")
    }
}

dependencies {
    implementation(projects.datasource.remote.auth.api)
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
}
