import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
    id("memozy.hilt")
}

setNamespace("data.backup.impl")

dependencies {
    implementation(projects.data.backup.api)
    implementation(projects.datasource.local.memo.api)
    implementation(projects.datasource.local.chat.api)
    implementation(projects.datasource.remote.auth.api)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.room.ktx)
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
}
