package me.pecos.memozy.data.backup

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.pecos.memozy.data.datasource.local.CategoryDao
import me.pecos.memozy.data.datasource.local.MemoDao
import me.pecos.memozy.data.datasource.local.chat.ChatMessageDao
import me.pecos.memozy.data.datasource.local.chat.ChatSessionDao
import me.pecos.memozy.data.datasource.local.entity.Category
import me.pecos.memozy.data.datasource.local.entity.Memo
import me.pecos.memozy.data.datasource.local.chat.entity.ChatSession
import me.pecos.memozy.data.datasource.local.chat.entity.ChatMessage
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
) : BackupRepository {

    override suspend fun createBackupPayload(): BackupPayload {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

        val memos = memoDao.getAllMemosForBackup().map { it.toBackup() }
        val categories = categoryDao.getAllCategoriesOnce().map { it.toBackup() }
        val chatSessions = chatSessionDao.getAllSessionsOnce().map { it.toBackup() }
        val chatMessages = chatMessageDao.getAllMessagesOnce().map { it.toBackup() }

        return BackupPayload(
            deviceName = android.os.Build.MODEL,
            appVersion = packageInfo.versionName ?: "unknown",
            dbVersion = 17,
            tables = BackupTables(
                memos = memos,
                categories = categories,
                chatSessions = chatSessions.ifEmpty { null },
                chatMessages = chatMessages.ifEmpty { null },
            ),
        )
    }

    override suspend fun restoreFromPayload(payload: BackupPayload) {
        // 복원 순서: FK 의존성 고려
        // 1. 기존 데이터 삭제 (역순)
        chatMessageDao.clearAllMessages()
        chatSessionDao.clearAllSessions()
        memoDao.clearAllMemos()
        categoryDao.clearAllCategories()

        // 2. 카테고리 먼저
        val categories = payload.tables.categories.map { it.toEntity() }
        if (categories.isNotEmpty()) categoryDao.insertCategories(categories)

        // 3. 메모
        val memos = payload.tables.memos.map { it.toEntity() }
        if (memos.isNotEmpty()) memoDao.insertMemos(memos)

        // 4. 채팅 세션
        val sessions = payload.tables.chatSessions?.map { it.toEntity() }
        if (!sessions.isNullOrEmpty()) chatSessionDao.insertSessions(sessions)

        // 5. 채팅 메시지
        val messages = payload.tables.chatMessages?.map { it.toEntity() }
        if (!messages.isNullOrEmpty()) chatMessageDao.insertMessages(messages)
    }
}

// --- Entity → Backup 변환 ---

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

// --- Backup → Entity 변환 ---

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
