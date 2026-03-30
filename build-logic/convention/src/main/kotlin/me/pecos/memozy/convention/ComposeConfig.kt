package me.pecos.memozy.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import me.pecos.memozy.convention.extension.libs

/**
 * Compose Multiplatform 기본 설정 (라이브러리용)
 */
fun Project.configureComposeLibrary() {
    with(pluginManager) {
        apply("org.jetbrains.compose")
        apply("org.jetbrains.kotlin.plugin.compose")
    }

    val composeDeps = extensions.getByType(ComposeExtension::class.java).dependencies

    extensions.configure<KotlinMultiplatformExtension> {
        sourceSets.apply {
            commonMain.dependencies {
                implementation(composeDeps.runtime)
                implementation(composeDeps.foundation)
                implementation(composeDeps.ui)
                implementation(composeDeps.material3)
                implementation(composeDeps.materialIconsExtended)
                api(composeDeps.components.resources)
                implementation(composeDeps.components.uiToolingPreview)
            }
        }
    }

    dependencies {
        add("androidRuntimeClasspath", composeDeps.uiTooling)
    }

    tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).configureEach {
        compilerOptions {
            freeCompilerArgs.addAll(
                listOf(
                    "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                    "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
                )
            )
        }
    }
}

/**
 * Compose Multiplatform Feature 설정 (ViewModel, Lifecycle 포함)
 */
fun Project.configureComposeFeature() {
    configureComposeLibrary()

    extensions.configure<KotlinMultiplatformExtension> {
        sourceSets.apply {
            commonMain.dependencies {
                implementation(libs.findLibrary("androidx.lifecycle.viewmodelCompose").get())
                implementation(libs.findLibrary("androidx.lifecycle.runtimeCompose").get())
            }

            androidMain.dependencies {
                implementation(libs.findLibrary("androidx.activity.compose").get())
            }
        }
    }
}
