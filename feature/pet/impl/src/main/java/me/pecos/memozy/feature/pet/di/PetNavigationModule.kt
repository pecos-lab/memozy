package me.pecos.memozy.feature.pet.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.pecos.memozy.feature.pet.PetNavigation
import me.pecos.memozy.feature.pet.PetNavigationImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PetNavigationModule {

    @Binds
    @Singleton
    abstract fun bindPetNavigation(impl: PetNavigationImpl): PetNavigation
}
