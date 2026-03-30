package me.pecos.memozy.convention.extension

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

private const val BASE_NAMESPACE = "me.pecos.memozy"

fun Project.setNamespace(name: String) {
    (extensions.getByName("android") as CommonExtension).apply {
        namespace = if (name.isEmpty()) BASE_NAMESPACE else "$BASE_NAMESPACE.$name"
    }
}
