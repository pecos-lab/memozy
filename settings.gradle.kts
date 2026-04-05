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
include(":app")
include(":datasource:local:memo:api")
include(":datasource:local:memo:impl")
include(":datasource:local:chat:api")
include(":datasource:local:chat:impl")
include(":datasource:remote:ai:api")
include(":datasource:remote:ai:impl")
include(":data:repository:memo:api")
include(":data:repository:memo:impl")
include(":data:repository:chat:api")
include(":data:repository:chat:impl")
include(":feature:core:resource")
include(":feature:home:api")
include(":feature:home:impl")
include(":feature:memo-plain:api")
include(":feature:memo-plain:impl")
