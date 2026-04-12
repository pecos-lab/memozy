package me.pecos.memozy.data.backup

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import me.pecos.memozy.data.datasource.local.CategoryDao
import me.pecos.memozy.data.datasource.local.MemoDao
import me.pecos.memozy.data.datasource.local.chat.ChatMessageDao
import me.pecos.memozy.data.datasource.local.chat.ChatSessionDao
import me.pecos.memozy.data.datasource.local.chat.entity.ChatMessage
import me.pecos.memozy.data.datasource.local.chat.entity.ChatSession
import me.pecos.memozy.data.datasource.local.entity.Category
import me.pecos.memozy.data.datasource.local.entity.Memo
import me.pecos.memozy.data.backup.di.BackupHttpClient
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
    private val authService: AuthService,
    @BackupHttpClient private val httpClient: HttpClient,
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

    // --- Cloud operations ---

    private fun authHeader(): String {
        val token = authService.getAccessToken()
            ?: throw IllegalStateException("Not authenticated")
        return "Bearer $token"
    }

    override suspend fun uploadBackup(): Result<BackupCreateResponse> = runCatching {
        val payload = createBackupPayload()
        val request = BackupUploadRequest(
            device_name = payload.deviceName,
            app_version = payload.appVersion,
            db_version = payload.dbVersion,
            tables = payload.tables,
        )
        httpClient.post("backup") {
            header("Authorization", authHeader())
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<BackupCreateResponse>()
    }

    override suspend fun listBackups(): Result<List<BackupMeta>> = runCatching {
        httpClient.get("backups") {
            header("Authorization", authHeader())
        }.body<List<BackupMeta>>()
    }

    override suspend fun downloadBackup(backupId: String): Result<BackupDownloadResponse> = runCatching {
        httpClient.get("backup/$backupId") {
            header("Authorization", authHeader())
        }.body<BackupDownloadResponse>()
    }

    override suspend fun deleteBackup(backupId: String): Result<Unit> = runCatching {
        httpClient.delete("backup/$backupId") {
            header("Authorization", authHeader())
        }
        Unit
    }

    override suspend fun restoreFromCloud(backupId: String): Result<Int> = runCatching {
        val response = downloadBackup(backupId).getOrThrow()
        restoreFromPayload(BackupPayload(
            deviceName = response.metadata.device_name,
            appVersion = response.metadata.app_version,
            dbVersion = response.metadata.db_version,
            tables = response.tables,
        ))
        response.metadata.memo_count
    }
}

// --- Entity → Backup ---

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

// --- Backup → Entity ---

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
