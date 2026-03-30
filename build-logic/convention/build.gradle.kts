plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

group = "me.pecos.memozy.buildlogic"

val javaVersion = JavaVersion.VERSION_21

java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
    }
}

dependencies {
    implementation(libs.gradlePlugin.android)
    implementation(libs.gradlePlugin.kotlin)
    implementation(libs.gradlePlugin.hilt)
    implementation(libs.gradlePlugin.ksp)
    implementation(libs.gradlePlugin.compose)
    implementation(libs.gradlePlugin.composeCompiler)
    implementation(libs.gradlePlugin.kotlinSerialization)
}
