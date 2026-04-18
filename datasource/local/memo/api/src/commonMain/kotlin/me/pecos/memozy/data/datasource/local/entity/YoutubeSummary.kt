package me.pecos.memozy.data.datasource.local.entity

import androidx.room.Entity
import kotlin.time.Clock

@Entity(tableName = "youtube_summary", primaryKeys = ["videoId", "mode", "language"])
data class YoutubeSummary(
    val videoId: String,
    val mode: String = "SIMPLE",
    val language: String = "ko",
    val url: String,
    val summary: String,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds()
)
