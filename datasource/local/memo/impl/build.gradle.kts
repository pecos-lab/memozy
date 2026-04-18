plugins {
    id("memozy.kmp.library")
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.datasource.local.memo.impl"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.datasource.local.memo.api)
            implementation(projects.datasource.local.chat.api)
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

// Room KMP (KSP2): 플랫폼별 KSP로 `actual object MemoDatabaseConstructor`와 `MemoDatabase_Impl`을 각 target에 직접 생성.
// - commonMain: @Database + `expect object` 만 선언, 수동 actual 없음.
// - kspAndroid: Android target에 자동 wire → compileAndroidMain이 clearAllTables 등 포함된 완전한 _Impl 본다.
// - kspIos*: macOS 호스트에서만 실행 (Kotlin/Native KSP는 Apple 호스트 필요). CI(kmp-ios-check.yml)가 macos-latest에서 돌림.
// - Windows 로컬에서는 iOS KSP가 SKIPPED → iOS compile 불가, 하지만 Android 개발에는 지장 없음.
dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosX64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}
