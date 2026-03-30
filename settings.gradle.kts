enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "memozy"
include(":androidApp:memozy")
include(":composeApp:host")
include(":composeApp:datasource:local:memo:api")
include(":composeApp:datasource:local:memo:impl")
include(":composeApp:data:repository:memo:api")
include(":composeApp:data:repository:memo:impl")
include(":composeApp:feature:core:resource")
include(":composeApp:feature:home:api")
include(":composeApp:feature:home:impl")
include(":composeApp:feature:memo-plain:api")
include(":composeApp:feature:memo-plain:impl")
