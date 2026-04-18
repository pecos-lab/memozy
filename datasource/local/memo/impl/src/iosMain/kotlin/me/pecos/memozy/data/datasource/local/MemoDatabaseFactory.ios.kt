package me.pecos.memozy.data.datasource.local

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual class MemoDatabaseFactory {
    @OptIn(ExperimentalForeignApi::class)
    actual fun create(): RoomDatabase.Builder<MemoDatabase> {
        val documentsUrl = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        val dbPath = "${documentsUrl?.path}/$MEMO_DB_NAME"
        return Room.databaseBuilder<MemoDatabase>(
            name = dbPath
        ).setDriver(BundledSQLiteDriver())
    }
}
