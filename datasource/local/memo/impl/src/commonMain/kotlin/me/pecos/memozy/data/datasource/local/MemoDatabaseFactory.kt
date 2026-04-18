package me.pecos.memozy.data.datasource.local

import androidx.room.RoomDatabase

expect class MemoDatabaseFactory {
    fun create(): RoomDatabase.Builder<MemoDatabase>
}

internal const val MEMO_DB_NAME = "memo_database"
