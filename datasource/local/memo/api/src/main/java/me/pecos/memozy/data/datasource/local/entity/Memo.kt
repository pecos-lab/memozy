package me.pecos.memozy.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import me.pecos.memozy.data.repository.model.MemoFormat

@Entity(tableName = "memo")
data class Memo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val categoryId: Int,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val format: MemoFormat = MemoFormat.PLAIN,
    val isPinned: Boolean = false,
    val audioPath: String? = null,
    val styles: String? = null, // JSON: [{start,end,bold,italic,strikethrough,color}]
    val youtubeUrl: String? = null,
    val deletedAt: Long? = null,
    val reminderAt: Long? = null,
    val summaryContent: String? = null,
    val isSummaryExpanded: Boolean = true
)
