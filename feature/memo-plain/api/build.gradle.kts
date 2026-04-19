plugins {
    id("memozy.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.feature.memoplain.api"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.jetbrains.navigation.compose)
        }
    }
}
