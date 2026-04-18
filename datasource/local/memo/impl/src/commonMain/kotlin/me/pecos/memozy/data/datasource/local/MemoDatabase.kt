package me.pecos.memozy.data.datasource.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import me.pecos.memozy.data.datasource.local.chat.ChatMessageDao
import me.pecos.memozy.data.datasource.local.chat.ChatSessionDao
import me.pecos.memozy.data.datasource.local.chat.entity.ChatMessage
import me.pecos.memozy.data.datasource.local.chat.entity.ChatSession
import me.pecos.memozy.data.datasource.local.converter.MemoFormatConverter
import me.pecos.memozy.data.datasource.local.entity.AiUsage
import me.pecos.memozy.data.datasource.local.entity.Category
import me.pecos.memozy.data.datasource.local.entity.Memo
import me.pecos.memozy.data.datasource.local.entity.YoutubeSummary

@Database(
    entities = [
        Memo::class,
        Category::class,
        ChatSession::class,
        ChatMessage::class,
        YoutubeSummary::class,
        AiUsage::class
    ],
    version = MEMO_DB_VERSION,
    exportSchema = true
)
@TypeConverters(MemoFormatConverter::class)
@ConstructedBy(MemoDatabaseConstructor::class)
abstract class MemoDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao
    abstract fun categoryDao(): CategoryDao
    abstract fun chatSessionDao(): ChatSessionDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun youtubeSummaryDao(): YoutubeSummaryDao
    abstract fun aiUsageDao(): AiUsageDao
}

@Suppress("KotlinNoActualForExpect")
expect object MemoDatabaseConstructor : RoomDatabaseConstructor<MemoDatabase> {
    override fun initialize(): MemoDatabase
}
