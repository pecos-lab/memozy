package me.pecos.memozy.data.datasource.local.pet.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pet_history")
data class PetHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val speciesId: String,
    val name: String,
    val personality: String,
    val rarity: Int,
    val level: Int,
    val daysTogether: Int,
    val memosWritten: Int,
    val departedAt: Long = System.currentTimeMillis()
)
