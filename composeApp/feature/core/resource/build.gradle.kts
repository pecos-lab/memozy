import me.pecos.memozy.convention.extension.configureAndroidLibrary
import org.jetbrains.compose.ComposeExtension

plugins {
    id("memozy.compose.library")
}

configureAndroidLibrary("feature.core.resource")

extensions.configure<ComposeExtension> {
    resources {
        publicResClass = true
        packageOfResClass = "me.pecos.memozy.feature.core.resource"
    }
}
