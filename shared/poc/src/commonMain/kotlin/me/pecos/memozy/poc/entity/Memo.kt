package me.pecos.memozy.poc.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import me.pecos.memozy.poc.model.MemoFormat

@Entity(tableName = "memo")
data class Memo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val categoryId: Int,
    val content: String,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val format: MemoFormat = MemoFormat.PLAIN,
    val isPinned: Boolean = false,
    val audioPath: String? = null,
    val styles: String? = null,
    val youtubeUrl: String? = null,
    val deletedAt: Long? = null,
    val reminderAt: Long? = null,
    val summaryContent: String? = null,
    val isSummaryExpanded: Boolean = true
)
