package me.pecos.memozy.convention

import com.android.build.api.dsl.ApplicationExtension
import me.pecos.memozy.convention.extension.AndroidExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal const val COMPILE_SDK = 36
internal const val MIN_SDK = 26
internal val JAVA_VERSION = JavaVersion.VERSION_17
internal val JVM_TARGET = JvmTarget.JVM_17

internal fun ApplicationExtension.configureApplication() {
    defaultConfig {
        targetSdk = COMPILE_SDK
    }
}

internal fun AndroidExtension.configureAndroid() {
    compileSdk = COMPILE_SDK
    defaultConfig {
        minSdk = MIN_SDK
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JAVA_VERSION
        targetCompatibility = JAVA_VERSION
    }
}

internal fun Project.configureKotlin() {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JVM_TARGET)
        }
    }
}
