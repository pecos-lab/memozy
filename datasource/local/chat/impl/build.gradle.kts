import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
    id("memozy.hilt")
}

setNamespace("datasource.local.chat.impl")

dependencies {
    implementation(projects.datasource.local.chat.api)
    implementation(projects.datasource.local.memo.impl)
}
