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
            // TODO(#294 Phase 3): RevenueCat 도입 후 billing-ktx 제거. 현재는
            // AndroidBillingService 가 Google Billing 을 직접 호출하고 있어
            // purchases-kmp-core 와 일시 공존. RevenueCat 으로 교체 시 정리.
            implementation(libs.billing.ktx)
            implementation(libs.kotlinx.coroutines.android)
        }
    }
}
