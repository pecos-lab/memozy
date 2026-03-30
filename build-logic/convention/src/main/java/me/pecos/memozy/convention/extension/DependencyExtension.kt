package me.pecos.memozy.convention.extension

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

typealias AndroidExtension = CommonExtension<*, *, *, *, *, *>

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.android(block: AndroidExtension.() -> Unit) {
    extensions.getByType<AndroidExtension>().apply(block)
}

private const val BASE_NAMESPACE = "me.pecos.memozy"

fun Project.setNamespace(name: String) {
    (extensions.getByName("android") as AndroidExtension).apply {
        namespace = if (name.isEmpty()) BASE_NAMESPACE else "$BASE_NAMESPACE.$name"
    }
}
