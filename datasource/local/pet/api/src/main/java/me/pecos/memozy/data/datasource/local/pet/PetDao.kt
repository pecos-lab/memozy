package me.pecos.memozy.data.datasource.local.pet

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.pecos.memozy.data.datasource.local.pet.entity.Pet
import me.pecos.memozy.data.datasource.local.pet.entity.PetHistory

@Dao
interface PetDao {

    @Query("SELECT * FROM pet WHERE isActive = 1 LIMIT 1")
    fun getActivePet(): Flow<Pet?>

    @Insert
    suspend fun insertPet(pet: Pet)

    @Update
    suspend fun updatePet(pet: Pet)

    @Query("UPDATE pet SET isActive = 0 WHERE id = :id")
    suspend fun deactivatePet(id: Int)

    @Query("SELECT * FROM pet_history ORDER BY departedAt DESC")
    fun getPetHistory(): Flow<List<PetHistory>>

    @Insert
    suspend fun insertPetHistory(history: PetHistory)
}
