package me.pecos.memozy.convention

import me.pecos.memozy.convention.extension.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureHiltAndroid() {
    with(pluginManager) {
        apply("com.google.devtools.ksp")
        apply("dagger.hilt.android.plugin")
    }

    dependencies {
        add("implementation", libs.findLibrary("hilt-android").get())
        add("ksp", libs.findLibrary("hilt-compiler").get())
    }
}
