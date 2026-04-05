package me.pecos.memozy.convention

import me.pecos.memozy.convention.extension.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureKtorAndroid() {
    with(pluginManager) {
        apply("org.jetbrains.kotlin.plugin.serialization")
    }

    dependencies {
        add("implementation", libs.findLibrary("ktor-client-core").get())
        add("implementation", libs.findLibrary("ktor-client-okhttp").get())
        add("implementation", libs.findLibrary("ktor-client-content-negotiation").get())
        add("implementation", libs.findLibrary("ktor-serialization-kotlinx-json").get())
        add("implementation", libs.findLibrary("ktor-client-logging").get())
        add("implementation", libs.findLibrary("kotlinx-serialization-json").get())
    }
}
