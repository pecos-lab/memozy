package me.pecos.memozy.convention

import me.pecos.memozy.convention.extension.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureRoomAndroid() {
    with(pluginManager) {
        apply("com.google.devtools.ksp")
    }

    dependencies {
        add("implementation", libs.findLibrary("room-runtime").get())
        add("implementation", libs.findLibrary("room-ktx").get())
        add("ksp", libs.findLibrary("room-compiler").get())
    }
}
