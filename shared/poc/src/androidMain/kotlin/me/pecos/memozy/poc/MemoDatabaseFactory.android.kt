package me.pecos.memozy.poc

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

actual class MemoDatabaseFactory(private val context: Context) {
    actual fun create(): RoomDatabase.Builder<MemoDatabase> {
        val dbFile = context.getDatabasePath(DB_NAME)
        return Room.databaseBuilder<MemoDatabase>(
            context = context.applicationContext,
            name = dbFile.absolutePath
        ).setDriver(BundledSQLiteDriver())
    }

    companion object {
        const val DB_NAME = "memo_database"
    }
}
