package me.pecos.nota.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import me.pecos.nota.data.reposotory.model.MemoFormat

@Entity(tableName = "memo")
data class Memo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val categoryId: Int,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val format: MemoFormat = MemoFormat.PLAIN
)