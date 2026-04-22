plugins {
    id("memozy.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "me.pecos.memozy.data.repository.subscription.impl"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.data.repository.subscription.api)
            implementation(projects.datasource.remote.subscription.api)
            implementation(projects.feature.core.resource)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.junit)
            implementation(libs.mockk)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
