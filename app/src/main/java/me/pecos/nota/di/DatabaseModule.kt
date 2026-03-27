package me.pecos.nota.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
            .build()
    }

    @Provides
    fun provideMemoDao(database: MemoDatabase): MemoDao {
        return database.memoDao()
    }
}
