package me.pecos.nota.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import me.pecos.nota.MemoDao
import me.pecos.nota.MemoDatabase
import me.pecos.nota.MIGRATION_1_2
import me.pecos.nota.MIGRATION_2_3
import me.pecos.nota.MIGRATION_3_4
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMemoDatabase(@ApplicationContext context: Context): MemoDatabase {
        return Room.databaseBuilder(
            context,
            MemoDatabase::class.java,
            "memo_database"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    db.execSQL("INSERT INTO `category` (`id`, `name`) VALUES (1,'일반'),(2,'업무'),(3,'아이디어'),(4,'할 일'),(5,'공부'),(6,'일정'),(7,'가계부'),(8,'운동'),(9,'건강'),(10,'여행'),(11,'쇼핑'),(12,'미분류')")
                }
            })
            .build()
    }

    @Provides
    fun provideMemoDao(database: MemoDatabase): MemoDao {
        return database.memoDao()
    }
}
