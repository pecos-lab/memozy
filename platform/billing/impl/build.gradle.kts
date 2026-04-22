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
            implementation(projects.data.repository.subscription.api)
            implementation(projects.data.repository.user.api)
        }
        androidMain.dependencies {
            implementation(libs.billing.ktx)
            implementation(libs.kotlinx.coroutines.android)
        }
    }
}
