package me.pecos.nota

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Memo::class], version = 1, exportSchema = false)
abstract class MemoDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao

    companion object {
        @Volatile
        private var INSTANCE: MemoDatabase? = null

        fun getDatabase(context: Context): MemoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MemoDatabase::class.java,
                    "memo_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
