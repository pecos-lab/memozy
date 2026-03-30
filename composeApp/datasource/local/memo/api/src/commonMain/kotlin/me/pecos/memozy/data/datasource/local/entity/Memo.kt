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
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val format: MemoFormat = MemoFormat.PLAIN
)
