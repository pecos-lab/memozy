package me.pecos.memozy.poc

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import me.pecos.memozy.poc.converter.MemoFormatConverter
import me.pecos.memozy.poc.dao.AiUsageDao
import me.pecos.memozy.poc.dao.CategoryDao
import me.pecos.memozy.poc.dao.ChatMessageDao
import me.pecos.memozy.poc.dao.ChatSessionDao
import me.pecos.memozy.poc.dao.MemoDao
import me.pecos.memozy.poc.dao.YoutubeSummaryDao
import me.pecos.memozy.poc.entity.AiUsage
import me.pecos.memozy.poc.entity.Category
import me.pecos.memozy.poc.entity.ChatMessage
import me.pecos.memozy.poc.entity.ChatSession
import me.pecos.memozy.poc.entity.Memo
import me.pecos.memozy.poc.entity.YoutubeSummary

@Database(
    entities = [
        Memo::class,
        Category::class,
        ChatSession::class,
        ChatMessage::class,
        YoutubeSummary::class,
        AiUsage::class
    ],
    version = 18,
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
