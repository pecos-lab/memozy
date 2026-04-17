package me.pecos.memozy.poc

import androidx.room.RoomDatabase

/**
 * Platform-specific factory for [MemoDatabase].
 * Android: needs Context (constructed in androidMain actual).
 * iOS: uses NSDocumentDirectory-based path.
 */
expect class MemoDatabaseFactory {
    fun create(): RoomDatabase.Builder<MemoDatabase>
}
