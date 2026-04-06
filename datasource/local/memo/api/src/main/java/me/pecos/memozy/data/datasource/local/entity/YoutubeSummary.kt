package me.pecos.memozy.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "youtube_summary")
data class YoutubeSummary(
    @PrimaryKey
    val videoId: String,
    val url: String,
    val summary: String,
    val createdAt: Long = System.currentTimeMillis()
)
