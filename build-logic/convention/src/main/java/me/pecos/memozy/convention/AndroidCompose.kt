package me.pecos.memozy.convention

import me.pecos.memozy.convention.extension.AndroidExtension
import me.pecos.memozy.convention.extension.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureComposeAndroid() {
    pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

    android {
        buildFeatures {
            compose = true
        }
    }

    dependencies {
        add("implementation", platform(libs.findLibrary("androidx-compose-bom").get()))
        add("implementation", libs.findLibrary("androidx-compose-ui").get())
        add("implementation", libs.findLibrary("androidx-compose-ui-graphics").get())
        add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
        add("implementation", libs.findLibrary("androidx-compose-material3").get())
        add("implementation", libs.findLibrary("androidx-compose-foundation").get())
        add("implementation", libs.findLibrary("androidx-compose-foundation-layout").get())
        add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
    }
}

private fun Project.android(block: AndroidExtension.() -> Unit) {
    extensions.configure(AndroidExtension::class.java, block)
}
