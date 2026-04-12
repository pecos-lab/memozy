package me.pecos.memozy.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupPayload(
    val deviceName: String,
    val appVersion: String,
    val dbVersion: Int,
    val tables: BackupTables,
)

@Serializable
data class BackupTables(
    val memos: List<MemoBackup>,
    val categories: List<CategoryBackup>,
    val chatSessions: List<ChatSessionBackup>? = null,
    val chatMessages: List<ChatMessageBackup>? = null,
)

@Serializable
data class MemoBackup(
    val id: Int,
    val name: String,
    val categoryId: Int,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val format: String = "PLAIN",
    val isPinned: Boolean = false,
    val audioPath: String? = null,
    val styles: String? = null,
    val youtubeUrl: String? = null,
    val deletedAt: Long? = null,
    val reminderAt: Long? = null,
    val summaryContent: String? = null,
)

@Serializable
data class CategoryBackup(
    val id: Int,
    val name: String,
)

@Serializable
data class ChatSessionBackup(
    val id: Int,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val category: String = "general",
)

@Serializable
data class ChatMessageBackup(
    val id: Int,
    val sessionId: Int,
    val role: String,
    val content: String,
    val timestamp: Long,
    val metadata: String? = null,
)

// --- Cloud backup API request ---

@Serializable
data class BackupUploadRequest(
    val device_name: String,
    val app_version: String,
    val db_version: Int,
    val tables: BackupTables,
)

// --- Cloud backup metadata ---

@Serializable
data class BackupMeta(
    val id: String,
    val device_name: String,
    val app_version: String,
    val db_version: Int,
    val memo_count: Int,
    val size_bytes: Long,
    val created_at: String,
)

@Serializable
data class BackupCreateResponse(
    val id: String,
    val created_at: String,
    val memo_count: Int,
)

@Serializable
data class BackupDownloadResponse(
    val id: String,
    val metadata: BackupMeta,
    val tables: BackupTables,
)
