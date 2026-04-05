import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
    id("memozy.hilt")
}

setNamespace("datasource.local.user.impl")

dependencies {
    implementation(projects.datasource.local.user.api)
    implementation(projects.datasource.local.memo.impl)
    implementation(libs.datastore.preferences)
}
