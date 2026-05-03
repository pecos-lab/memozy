plugins {
    id("memozy.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.platform.billing.impl"
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.platform.billing.api)
            implementation(libs.purchases.kmp.core)
            implementation(libs.purchases.kmp.result)
        }
        androidMain.dependencies {
            implementation(libs.billing.ktx)
            implementation(libs.kotlinx.coroutines.android)
        }
    }
}
