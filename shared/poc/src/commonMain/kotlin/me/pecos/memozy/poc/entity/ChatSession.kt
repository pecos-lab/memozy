package me.pecos.memozy.poc.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_session")
data class ChatSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val category: String = "general"
)
