package me.pecos.memozy.data.datasource.local.user.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievement")
data class Achievement(
    @PrimaryKey
    val id: String,
    val unlockedAt: Long,
    val title: String,
    val description: String
)
