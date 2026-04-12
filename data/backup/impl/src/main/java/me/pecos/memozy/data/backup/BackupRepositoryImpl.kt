package me.pecos.memozy.data.backup

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import me.pecos.memozy.data.datasource.local.AiUsageDao
import me.pecos.memozy.data.datasource.local.CategoryDao
import me.pecos.memozy.data.datasource.local.MemoDao
import me.pecos.memozy.data.datasource.local.YoutubeSummaryDao
import me.pecos.memozy.data.datasource.local.chat.ChatMessageDao
import me.pecos.memozy.data.datasource.local.chat.ChatSessionDao
import me.pecos.memozy.data.datasource.local.chat.entity.ChatMessage
import me.pecos.memozy.data.datasource.local.chat.entity.ChatSession
import me.pecos.memozy.data.datasource.local.entity.AiUsage
import me.pecos.memozy.data.datasource.local.entity.Category
import me.pecos.memozy.data.datasource.local.entity.Memo
import me.pecos.memozy.data.datasource.local.entity.YoutubeSummary
import me.pecos.memozy.data.datasource.remote.auth.AuthService
import me.pecos.memozy.data.repository.model.MemoFormat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val memoDao: MemoDao,
    private val categoryDao: CategoryDao,
    private val chatSessionDao: ChatSessionDao,
    private val chatMessageDao: ChatMessageDao,
    private val youtubeSummaryDao: YoutubeSummaryDao,
    private val aiUsageDao: AiUsageDao,
    private val authService: AuthService,
    private val supabaseClient: SupabaseClient,
) : BackupRepository {

    private fun userId(): String =
        authService.currentUser?.id ?: throw IllegalStateException("Not authenticated")

    // ── Cloud Upload (upsert) ──

    override suspend fun uploadBackup(): Result<Int> = runCatching {
        val uid = userId()
        var totalRows = 0

        // 1. Categories
        val categories = categoryDao.getAllCategoriesOnce()
        if (categories.isNotEmpty()) {
            val rows = categories.map { it.toSupa(uid) }
            supabaseClient.from("category").upsert(rows) { onConflict = "user_id,id" }
            totalRows += rows.size
        }

        // 2. Memos
        val memos = memoDao.getAllMemosForBackup()
        if (memos.isNotEmpty()) {
            val rows = memos.map { it.toSupa(uid) }
            supabaseClient.from("memo").upsert(rows) { onConflict = "user_id,id" }
            totalRows += rows.size
        }

        // 3. Chat Sessions
        val sessions = chatSessionDao.getAllSessionsOnce()
        if (sessions.isNotEmpty()) {
            val rows = sessions.map { it.toSupa(uid) }
            supabaseClient.from("chat_session").upsert(rows) { onConflict = "user_id,id" }
            totalRows += rows.size
        }

        // 4. Chat Messages
        val messages = chatMessageDao.getAllMessagesOnce()
        if (messages.isNotEmpty()) {
            val rows = messages.map { it.toSupa(uid) }
            supabaseClient.from("chat_message").upsert(rows) { onConflict = "user_id,id" }
            totalRows += rows.size
        }

        // 5. Youtube Summaries
        val summaries = youtubeSummaryDao.getAllOnce()
        if (summaries.isNotEmpty()) {
            val rows = summaries.map { it.toSupa(uid) }
            supabaseClient.from("youtube_summary").upsert(rows) { onConflict = "user_id,video_id,mode,language" }
            totalRows += rows.size
        }

        // 6. AI Usage
        val aiUsages = aiUsageDao.getAllOnce()
        if (aiUsages.isNotEmpty()) {
            val rows = aiUsages.map { it.toSupa(uid) }
            supabaseClient.from("ai_usage").upsert(rows) { onConflict = "user_id,id" }
            totalRows += rows.size
        }

        // 7. Backup metadata
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val metadata = SupaBackupMetadataInsert(
            userId = uid,
            deviceName = android.provider.Settings.Global.getString(
                context.contentResolver, "device_name"
            ) ?: android.os.Build.MODEL,
            appVersion = packageInfo.versionName ?: "unknown",
            dbVersion = 17,
            memoCount = memos.size,
            totalRows = totalRows,
        )
        supabaseClient.from("backup_metadata").insert(metadata)

        memos.size
    }

    // ── Cloud Restore ──

    override suspend fun restoreFromCloud(): Result<Int> = runCatching {
        val uid = userId()

        // 1. Download all data from Supabase
        val categories = supabaseClient.from("category")
            .select { filter { eq("user_id", uid) } }
            .decodeList<SupaCategory>()

        val memos = supabaseClient.from("memo")
            .select { filter { eq("user_id", uid) } }
            .decodeList<SupaMemo>()

        val sessions = supabaseClient.from("chat_session")
            .select { filter { eq("user_id", uid) } }
            .decodeList<SupaChatSession>()

        val messages = supabaseClient.from("chat_message")
            .select { filter { eq("user_id", uid) } }
            .decodeList<SupaChatMessage>()

        val summaries = supabaseClient.from("youtube_summary")
            .select { filter { eq("user_id", uid) } }
            .decodeList<SupaYoutubeSummary>()

        val aiUsages = supabaseClient.from("ai_usage")
            .select { filter { eq("user_id", uid) } }
            .decodeList<SupaAiUsage>()

        // 2. Clear local data (order matters for foreign keys)
        chatMessageDao.clearAllMessages()
        chatSessionDao.clearAllSessions()
        memoDao.clearAllMemos()
        categoryDao.clearAllCategories()
        youtubeSummaryDao.clearAll()
        aiUsageDao.clearAll()

        // 3. Insert downloaded data
        if (categories.isNotEmpty()) {
            categoryDao.insertCategories(categories.map { it.toEntity() })
        }
        if (memos.isNotEmpty()) {
            memoDao.insertMemos(memos.map { it.toEntity() })
        }
        if (sessions.isNotEmpty()) {
            chatSessionDao.insertSessions(sessions.map { it.toEntity() })
        }
        if (messages.isNotEmpty()) {
            chatMessageDao.insertMessages(messages.map { it.toEntity() })
        }
        if (summaries.isNotEmpty()) {
            youtubeSummaryDao.insertAll(summaries.map { it.toEntity() })
        }
        if (aiUsages.isNotEmpty()) {
            aiUsageDao.insertAll(aiUsages.map { it.toEntity() })
        }

        memos.size
    }

    // ── Last Backup Time ──

    override suspend fun getLastBackupTime(): Result<String?> = runCatching {
        val uid = userId()
        val result = supabaseClient.from("backup_metadata")
            .select {
                filter { eq("user_id", uid) }
                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                limit(1)
            }
            .decodeList<SupaBackupMetadata>()
        result.firstOrNull()?.createdAt
    }

    // ── Local JSON backup (legacy, for file export/import) ──

    override suspend fun createBackupPayload(): BackupPayload {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

        return BackupPayload(
            deviceName = android.provider.Settings.Global.getString(
                context.contentResolver, "device_name"
            ) ?: android.os.Build.MODEL,
            appVersion = packageInfo.versionName ?: "unknown",
            dbVersion = 17,
            tables = BackupTables(
                memos = memoDao.getAllMemosForBackup().map { it.toBackup() },
                categories = categoryDao.getAllCategoriesOnce().map { it.toBackup() },
                chatSessions = chatSessionDao.getAllSessionsOnce().map { it.toBackup() }.ifEmpty { null },
                chatMessages = chatMessageDao.getAllMessagesOnce().map { it.toBackup() }.ifEmpty { null },
            ),
        )
    }

    override suspend fun restoreFromPayload(payload: BackupPayload) {
        chatMessageDao.clearAllMessages()
        chatSessionDao.clearAllSessions()
        memoDao.clearAllMemos()
        categoryDao.clearAllCategories()

        val categories = payload.tables.categories.map { it.toEntity() }
        if (categories.isNotEmpty()) categoryDao.insertCategories(categories)

        val memos = payload.tables.memos.map { it.toEntity() }
        if (memos.isNotEmpty()) memoDao.insertMemos(memos)

        val sessions = payload.tables.chatSessions?.map { it.toEntity() }
        if (!sessions.isNullOrEmpty()) chatSessionDao.insertSessions(sessions)

        val messages = payload.tables.chatMessages?.map { it.toEntity() }
        if (!messages.isNullOrEmpty()) chatMessageDao.insertMessages(messages)
    }
}

// ── Entity → Supabase DTO ──

private fun Memo.toSupa(userId: String) = SupaMemo(
    id = id, userId = userId, name = name, categoryId = categoryId, content = content,
    createdAt = createdAt, updatedAt = updatedAt, format = format.name,
    isPinned = isPinned, audioPath = audioPath, styles = styles,
    youtubeUrl = youtubeUrl, deletedAt = deletedAt, reminderAt = reminderAt,
    summaryContent = summaryContent,
)

private fun Category.toSupa(userId: String) = SupaCategory(
    id = id, userId = userId, name = name,
)

private fun ChatSession.toSupa(userId: String) = SupaChatSession(
    id = id, userId = userId, title = title,
    createdAt = createdAt, updatedAt = updatedAt, category = category,
)

private fun ChatMessage.toSupa(userId: String) = SupaChatMessage(
    id = id, userId = userId, sessionId = sessionId,
    role = role, content = content, timestamp = timestamp, metadata = metadata,
)

private fun YoutubeSummary.toSupa(userId: String) = SupaYoutubeSummary(
    userId = userId, videoId = videoId,
    mode = mode.takeIf { it.isNotBlank() } ?: "SIMPLE",
    language = language.takeIf { it.isNotBlank() } ?: "ko",
    url = url,
    summary = summary,
    createdAt = createdAt,
)

private fun AiUsage.toSupa(userId: String) = SupaAiUsage(
    id = id, userId = userId, feature = feature, usedAt = usedAt,
)

// ── Supabase DTO → Entity ──

private fun SupaMemo.toEntity() = Memo(
    id = id, name = name, categoryId = categoryId, content = content,
    createdAt = createdAt, updatedAt = updatedAt,
    format = try { MemoFormat.valueOf(format) } catch (_: Exception) { MemoFormat.PLAIN },
    isPinned = isPinned, audioPath = audioPath, styles = styles,
    youtubeUrl = youtubeUrl, deletedAt = deletedAt, reminderAt = reminderAt,
    summaryContent = summaryContent,
)

private fun SupaCategory.toEntity() = Category(id = id, name = name)

private fun SupaChatSession.toEntity() = ChatSession(
    id = id, title = title, createdAt = createdAt, updatedAt = updatedAt, category = category,
)

private fun SupaChatMessage.toEntity() = ChatMessage(
    id = id, sessionId = sessionId, role = role, content = content,
    timestamp = timestamp, metadata = metadata,
)

private fun SupaYoutubeSummary.toEntity() = YoutubeSummary(
    videoId = videoId, mode = mode, language = language,
    url = url, summary = summary, createdAt = createdAt,
)

private fun SupaAiUsage.toEntity() = AiUsage(
    id = id, feature = feature, usedAt = usedAt,
)

// ── Entity → Legacy Backup ──

private fun Memo.toBackup() = MemoBackup(
    id = id, name = name, categoryId = categoryId, content = content,
    createdAt = createdAt, updatedAt = updatedAt, format = format.name,
    isPinned = isPinned, audioPath = audioPath, styles = styles,
    youtubeUrl = youtubeUrl, deletedAt = deletedAt, reminderAt = reminderAt,
    summaryContent = summaryContent,
)

private fun Category.toBackup() = CategoryBackup(id = id, name = name)

private fun ChatSession.toBackup() = ChatSessionBackup(
    id = id, title = title, createdAt = createdAt, updatedAt = updatedAt, category = category,
)

private fun ChatMessage.toBackup() = ChatMessageBackup(
    id = id, sessionId = sessionId, role = role, content = content,
    timestamp = timestamp, metadata = metadata,
)

// ── Legacy Backup → Entity ──

private fun MemoBackup.toEntity() = Memo(
    id = id, name = name, categoryId = categoryId, content = content,
    createdAt = createdAt, updatedAt = updatedAt,
    format = try { MemoFormat.valueOf(format) } catch (_: Exception) { MemoFormat.PLAIN },
    isPinned = isPinned, audioPath = audioPath, styles = styles,
    youtubeUrl = youtubeUrl, deletedAt = deletedAt, reminderAt = reminderAt,
    summaryContent = summaryContent,
)

private fun CategoryBackup.toEntity() = Category(id = id, name = name)

private fun ChatSessionBackup.toEntity() = ChatSession(
    id = id, title = title, createdAt = createdAt, updatedAt = updatedAt, category = category,
)

private fun ChatMessageBackup.toEntity() = ChatMessage(
    id = id, sessionId = sessionId, role = role, content = content,
    timestamp = timestamp, metadata = metadata,
)
