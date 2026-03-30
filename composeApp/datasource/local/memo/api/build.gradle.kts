import me.pecos.memozy.convention.extension.configureAndroidLibrary

plugins {
    id("memozy.library")
}

configureAndroidLibrary("datasource.local.memo.api")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.room.runtime)
        }
    }
}
