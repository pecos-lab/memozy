package me.pecos.memozy.data.datasource.local.pet.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pet")
data class Pet(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val speciesId: String,
    val name: String,
    val personality: String,
    val favoriteCategoryId: Int,
    val dislike: String,
    val catchphrase: String,
    val rarity: Int,
    val mood: Int = 70,
    val exp: Int = 0,
    val level: Int = 1,
    val lastFedAt: Long = 0L,
    val lastInteractionAt: Long = 0L,
    val birthday: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
