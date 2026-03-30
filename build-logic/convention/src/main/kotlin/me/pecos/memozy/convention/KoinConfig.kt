package me.pecos.memozy.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import me.pecos.memozy.convention.extension.libs

fun Project.configureKoin() {
    extensions.configure<KotlinMultiplatformExtension> {
        sourceSets.apply {
            commonMain.dependencies {
                implementation(libs.findLibrary("koin.core").get())
                implementation(libs.findLibrary("koin.compose").get())
                implementation(libs.findLibrary("koin.compose.viewmodel").get())
            }

            androidMain.dependencies {
                implementation(libs.findLibrary("koin.android").get())
            }
        }
    }
}
