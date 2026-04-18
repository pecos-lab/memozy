plugins {
    id("memozy.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.feature.memoplain.api"
    }

    sourceSets {
        // MemoPlainRoute는 commonMain(플랫폼 무관), MemoPlainNavigation 인터페이스는
        // androidx.navigation.NavGraphBuilder에 의존하여 androidMain 전용.
        androidMain.dependencies {
            implementation(libs.androidx.compose.navigation)
        }
    }
}
