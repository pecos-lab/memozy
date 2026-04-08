package me.pecos.memozy.data.datasource.local.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.pecos.memozy.data.datasource.local.MIGRATION_1_2
import me.pecos.memozy.data.datasource.local.MIGRATION_2_3
import me.pecos.memozy.data.datasource.local.MIGRATION_3_4
import me.pecos.memozy.data.datasource.local.MIGRATION_4_5
import me.pecos.memozy.data.datasource.local.MIGRATION_5_6
import me.pecos.memozy.data.datasource.local.MIGRATION_6_7
import me.pecos.memozy.data.datasource.local.MIGRATION_7_8
import me.pecos.memozy.data.datasource.local.MIGRATION_8_9
import me.pecos.memozy.data.datasource.local.MIGRATION_9_10
import me.pecos.memozy.data.datasource.local.MIGRATION_10_11
import me.pecos.memozy.data.datasource.local.AiUsageDao
import me.pecos.memozy.data.datasource.local.MemoDao
import me.pecos.memozy.data.datasource.local.MemoDatabase
import me.pecos.memozy.data.datasource.local.TagDao
import me.pecos.memozy.data.datasource.local.YoutubeSummaryDao
import me.pecos.memozy.data.datasource.local.chat.ChatMessageDao
import me.pecos.memozy.data.datasource.local.chat.ChatSessionDao
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
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
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

    @Provides
    fun provideChatSessionDao(database: MemoDatabase): ChatSessionDao {
        return database.chatSessionDao()
    }

    @Provides
    fun provideChatMessageDao(database: MemoDatabase): ChatMessageDao {
        return database.chatMessageDao()
    }

    @Provides
    fun provideYoutubeSummaryDao(database: MemoDatabase): YoutubeSummaryDao {
        return database.youtubeSummaryDao()
    }

    @Provides
    fun provideAiUsageDao(database: MemoDatabase): AiUsageDao {
        return database.aiUsageDao()
    }

    @Provides
    fun provideTagDao(database: MemoDatabase): TagDao {
        return database.tagDao()
    }
}
