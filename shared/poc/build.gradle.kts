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

// kspCommonMainMetadata가 생성한 actual MemoDatabaseConstructor를 commonMain에서 제외.
// expect 선언이 commonMain에 있어 같은 source set에 actual이 들어오면 "expect/actual in same module" 컴파일 에러.
// 플랫폼별 actual은 src/{androidMain,iosMain}/.../MemoDatabaseConstructor.*.kt에 수동 작성 → MemoDatabase_Impl() 호출.
kotlin.sourceSets.named("commonMain") {
    kotlin.srcDir(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin"))
    kotlin.exclude("**/MemoDatabaseConstructor.kt")
}

tasks.matching { it.name != "kspCommonMainKotlinMetadata" && it.name.startsWith("ksp") }
    .configureEach { dependsOn("kspCommonMainKotlinMetadata") }

tasks.matching { it.name.startsWith("compileKotlin") || it.name == "sourcesJar" }
    .configureEach { dependsOn("kspCommonMainKotlinMetadata") }
