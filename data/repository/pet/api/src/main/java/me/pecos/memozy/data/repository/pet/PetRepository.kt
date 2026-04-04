package me.pecos.memozy.data.repository.pet

import kotlinx.coroutines.flow.Flow
import me.pecos.memozy.data.datasource.local.pet.entity.Pet
import me.pecos.memozy.data.datasource.local.pet.entity.PetHistory

interface PetRepository {
    fun getActivePet(): Flow<Pet?>
    suspend fun hatchPet(): Pet
    suspend fun rerollPet(): Pet
    suspend fun feedPet(memoCount: Int)
    suspend fun interactWithPet()
    suspend fun updateMood()
    suspend fun namePet(name: String)
    fun getPetHistory(): Flow<List<PetHistory>>
    suspend fun feedPetWithStreakBonus(memoCount: Int, consecutiveDays: Int)
}
