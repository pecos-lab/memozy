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
include(":datasource:remote:ai:api")
include(":datasource:remote:ai:impl")
include(":datasource:remote:auth:api")
include(":datasource:remote:auth:impl")
include(":data:repository:memo:api")
include(":data:repository:memo:impl")
include(":data:repository:chat:api")
include(":data:repository:chat:impl")
include(":data:repository:user:api")
include(":data:repository:user:impl")
include(":data:backup:api")
include(":data:backup:impl")
include(":feature:core:resource")
include(":feature:core:viewmodel")
include(":feature:home:api")
include(":feature:home:impl")
include(":feature:memo-plain:api")
include(":feature:memo-plain:impl")
include(":platform:billing:api")
include(":platform:billing:impl")
include(":platform:ads:api")
include(":platform:ads:impl")
include(":platform:credential:api")
include(":platform:credential:impl")
include(":platform:media:api")
include(":platform:media:impl")
include(":platform:htmltext:api")
include(":platform:htmltext:impl")
include(":shared:poc")
include(":shared:umbrella")
