package me.pecos.memozy.convention

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

object Constants {
    const val COMPILE_SDK = 36
    const val MIN_SDK = 26
    const val TARGET_SDK = 36

    val JAVA_VERSION = JavaVersion.VERSION_21
    val JVM_TARGET = JvmTarget.JVM_21

    const val NAMESPACE_PREFIX = "me.pecos.memozy"

    const val IOS_FRAMEWORK_NAME = "MemozyKit"

    val KOTLIN_OPT_INS = listOf(
        "kotlin.RequiresOptIn",
        "kotlinx.coroutines.ExperimentalCoroutinesApi",
        "kotlinx.coroutines.FlowPreview",
    )
}
