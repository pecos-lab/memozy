package me.pecos.memozy.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_usage")
data class AiUsage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val feature: String,
    val usedAt: Long = System.currentTimeMillis()
)
