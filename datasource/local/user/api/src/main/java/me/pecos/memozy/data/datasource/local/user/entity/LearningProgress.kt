package me.pecos.memozy.data.datasource.local.user.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "learning_progress")
data class LearningProgress(
    @PrimaryKey
    val guideId: String,
    val completed: Boolean = false,
    val completedAt: Long? = null,
    val score: Int? = null
)
