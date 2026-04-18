plugins {
    id("memozy.kmp.library")
    id("memozy.cmp.library")
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.feature.home.api"
    }

    sourceSets {
        // HomeRoute는 commonMain(plain Kotlin)에서 공유하고,
        // androidx.navigation-compose를 사용하는 HomeNavigation 인터페이스는
        // androidMain에 둔다 (2.8.x 라인은 KMP 미지원).
        androidMain.dependencies {
            implementation(libs.androidx.compose.navigation)
        }
    }
}
