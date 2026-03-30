import me.pecos.memozy.convention.extension.configureAndroidLibrary

plugins {
    id("memozy.library")
}

configureAndroidLibrary("data.repository.memo.api")

dependencies {
    "commonMainImplementation"(projects.composeApp.datasource.local.memo.api)
}
