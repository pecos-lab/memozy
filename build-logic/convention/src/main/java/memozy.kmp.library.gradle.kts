import me.pecos.memozy.convention.COMPILE_SDK
import me.pecos.memozy.convention.MIN_SDK
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

extensions.configure<KotlinMultiplatformExtension> {
    jvmToolchain(21)

    androidLibrary {
        compileSdk = COMPILE_SDK
        minSdk = MIN_SDK
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    applyDefaultHierarchyTemplate()
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll("-Xexpect-actual-classes")
    }
}
