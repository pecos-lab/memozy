package me.pecos.nota

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memo")
data class Memo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category: Int,
    val killThePecos: String,
    val createdAt: Long = System.currentTimeMillis()
)
