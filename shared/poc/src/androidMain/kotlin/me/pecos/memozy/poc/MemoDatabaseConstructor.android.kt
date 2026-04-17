package me.pecos.memozy.poc

import androidx.room.RoomDatabaseConstructor

actual object MemoDatabaseConstructor : RoomDatabaseConstructor<MemoDatabase> {
    actual override fun initialize(): MemoDatabase = MemoDatabase_Impl()
}
