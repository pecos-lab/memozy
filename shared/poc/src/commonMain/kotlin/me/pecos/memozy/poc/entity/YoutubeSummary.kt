package me.pecos.memozy.poc.entity

import androidx.room.Entity

@Entity(tableName = "youtube_summary", primaryKeys = ["videoId", "mode", "language"])
data class YoutubeSummary(
    val videoId: String,
    val mode: String = "SIMPLE",
    val language: String = "ko",
    val url: String,
    val summary: String,
    val createdAt: Long = 0L
)
