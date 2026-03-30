package me.pecos.memozy.convention.extension

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import me.pecos.memozy.convention.Constants

/**
 * KMP AndroidLibrary namespace + compileSdk 설정 (AGP 9.0+)
 */
fun KotlinMultiplatformExtension.configureAndroidLibrary(moduleName: String) {
    (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryExtension>("androidLibrary") {
        namespace = "${Constants.NAMESPACE_PREFIX}.$moduleName"
        compileSdk = Constants.COMPILE_SDK
    }
}

/**
 * Project 레벨 간편 버전
 */
fun Project.configureAndroidLibrary(moduleName: String) {
    extensions.configure<KotlinMultiplatformExtension> {
        configureAndroidLibrary(moduleName)
    }
}
