import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import me.pecos.memozy.convention.extension.configureKmpIos
import me.pecos.memozy.convention.extension.configureKotlinOptions
import me.pecos.memozy.convention.extension.libs

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.plugin.serialization")
}

// KMP iOS 타겟 설정
configureKmpIos()

// Kotlin Compiler Options 설정
configureKotlinOptions()

// SourceSets 기본 의존성
extensions.configure<KotlinMultiplatformExtension> {
    sourceSets.apply {
        commonMain.dependencies {
            implementation(libs.findLibrary("kotlinx.coroutines.core").get())
            implementation(libs.findLibrary("kotlinx.serialization.json").get())
            implementation(libs.findLibrary("kotlinx.collections.immutable").get())
        }

        commonTest.dependencies {
            implementation(libs.findLibrary("kotlin.test").get())
            implementation(libs.findLibrary("kotlinx.coroutines.test").get())
        }

        androidMain.dependencies {
            implementation(libs.findLibrary("kotlinx.coroutines.android").get())
        }
    }
}
