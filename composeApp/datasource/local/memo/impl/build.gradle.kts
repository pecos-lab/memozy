import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
    id("memozy.hilt")
    id("memozy.room")
}

setNamespace("datasource.local.memo.impl")

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(projects.composeApp.datasource.local.memo.api)
}
