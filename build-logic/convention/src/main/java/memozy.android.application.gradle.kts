import com.android.build.api.dsl.ApplicationExtension
import me.pecos.memozy.convention.configureAndroid
import me.pecos.memozy.convention.configureApplication
import me.pecos.memozy.convention.configureKotlin

plugins {
    id("com.android.application")
}

extensions.getByType<ApplicationExtension>().apply {
    configureApplication()
    configureAndroid()
}

configureKotlin()
