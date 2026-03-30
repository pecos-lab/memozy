import me.pecos.memozy.convention.extension.configureAndroidLibrary

plugins {
    id("memozy.library")
    id("memozy.room")
}

configureAndroidLibrary("datasource.local.memo.impl")

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.composeApp.datasource.local.memo.api)
        }

        androidMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.koin.android)
        }
    }
}
