import me.pecos.memozy.convention.extension.setNamespace

plugins {
    id("memozy.android.library")
    id("memozy.hilt")
}

setNamespace("datasource.local.pet.impl")

dependencies {
    implementation(projects.datasource.local.pet.api)
}
