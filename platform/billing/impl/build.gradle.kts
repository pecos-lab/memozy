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
            implementation(projects.data.repository.user.api)
            implementation(projects.datasource.remote.auth.api)
            implementation(libs.purchases.kmp.core)
            implementation(libs.purchases.kmp.result)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
