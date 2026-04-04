package me.pecos.memozy.data.repository.pet.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.pecos.memozy.data.repository.pet.PetRepository
import me.pecos.memozy.data.repository.pet.PetRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PetRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPetRepository(impl: PetRepositoryImpl): PetRepository
}
