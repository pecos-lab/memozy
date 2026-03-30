package me.pecos.memozy.convention.extension

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import me.pecos.memozy.convention.Constants

fun Project.configureKmpIos(
    frameworkName: String = Constants.IOS_FRAMEWORK_NAME,
) {
    extensions.configure<KotlinMultiplatformExtension> {
        iosX64 {
            binaries.framework {
                baseName = frameworkName
            }
        }

        iosArm64 {
            binaries.framework {
                baseName = frameworkName
            }
        }

        iosSimulatorArm64 {
            binaries.framework {
                baseName = frameworkName
            }
        }
    }
}

fun Project.configureKotlinOptions() {
    tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).configureEach {
        compilerOptions {
            jvmTarget.set(Constants.JVM_TARGET)
            freeCompilerArgs.addAll(Constants.KOTLIN_OPT_INS.map { "-opt-in=$it" })
        }
    }
}
