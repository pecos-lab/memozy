package me.pecos.memozy.convention

import me.pecos.memozy.convention.extension.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun Project.configureRoom() {
    with(pluginManager) {
        apply("com.google.devtools.ksp")
        apply("androidx.room")
    }

    extensions.configure<KotlinMultiplatformExtension> {
        sourceSets.apply {
            commonMain.dependencies {
                implementation(libs.findLibrary("room.runtime").get())
            }

            iosMain.dependencies {
                implementation(libs.findLibrary("sqlite.bundled").get())
            }
        }
    }

    // KSP Room compiler for each target
    dependencies {
        add("kspAndroid", libs.findLibrary("room.compiler").get())
        add("kspIosX64", libs.findLibrary("room.compiler").get())
        add("kspIosArm64", libs.findLibrary("room.compiler").get())
        add("kspIosSimulatorArm64", libs.findLibrary("room.compiler").get())
    }
}
