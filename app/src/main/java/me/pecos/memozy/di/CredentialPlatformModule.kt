package me.pecos.memozy.di

import me.pecos.memozy.platform.credential.CredentialService
import me.pecos.memozy.platform.credential.provideCredentialService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val credentialPlatformModule = module {
    single<CredentialService> { provideCredentialService(androidContext()) }
}
