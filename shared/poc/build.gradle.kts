plugins {
    id("memozy.kmp.library")
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.shared.poc"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspCommonMainMetadata", libs.room.compiler)
}

kotlin.sourceSets.named("commonMain") {
    kotlin.srcDir(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin"))
    kotlin.exclude("**/MemoDatabaseConstructor.kt")
}

tasks.matching { it.name != "kspCommonMainKotlinMetadata" && it.name.startsWith("ksp") }
    .configureEach { dependsOn("kspCommonMainKotlinMetadata") }

tasks.matching { it.name.startsWith("compileKotlin") || it.name == "sourcesJar" }
    .configureEach { dependsOn("kspCommonMainKotlinMetadata") }
