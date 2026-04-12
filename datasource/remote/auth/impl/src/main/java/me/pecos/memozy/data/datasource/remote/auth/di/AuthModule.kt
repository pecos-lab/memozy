package me.pecos.memozy.data.datasource.remote.auth.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json
import me.pecos.memozy.data.datasource.remote.auth.AuthService
import me.pecos.memozy.data.datasource.remote.auth.AuthServiceImpl
import me.pecos.memozy.datasource.remote.auth.impl.BuildConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthService(impl: AuthServiceImpl): AuthService

    companion object {
        @Provides
        @Singleton
        fun provideSupabaseClient(): SupabaseClient = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
        ) {
            defaultSerializer = KotlinXSerializer(Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
            })
            install(Auth)
            install(Postgrest)
        }
    }
}
