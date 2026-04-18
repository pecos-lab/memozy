package me.pecos.memozy.data.datasource.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

actual class MemoDatabaseFactory(private val context: Context) {
    actual fun create(): RoomDatabase.Builder<MemoDatabase> {
        val dbFile = context.getDatabasePath(MEMO_DB_NAME)
        return Room.databaseBuilder<MemoDatabase>(
            context = context.applicationContext,
            name = dbFile.absolutePath
        ).setDriver(BundledSQLiteDriver())
    }
}
