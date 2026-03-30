import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
}

setNamespace("datasource.local.memo.api")

dependencies {
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.kotlinx.coroutines.android)
}
