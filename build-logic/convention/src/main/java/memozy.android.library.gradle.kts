import com.android.build.api.dsl.LibraryExtension
import me.pecos.memozy.convention.configureAndroid
import me.pecos.memozy.convention.configureKotlin

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

extensions.getByType<LibraryExtension>().apply {
    configureAndroid()
}

configureKotlin()
