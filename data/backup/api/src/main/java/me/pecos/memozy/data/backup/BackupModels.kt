package me.pecos.memozy.data.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Supabase table DTOs (snake_case for PostgREST) ──

@Serializable
data class SupaMemo(
    val id: Int,
    @SerialName("user_id") val userId: String,
    val name: String,
    @SerialName("category_id") val categoryId: Int,
    val content: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long,
    val format: String = "PLAIN",
    @SerialName("is_pinned") val isPinned: Boolean = false,
    @SerialName("audio_path") val audioPath: String? = null,
    val styles: String? = null,
    @SerialName("youtube_url") val youtubeUrl: String? = null,
    @SerialName("deleted_at") val deletedAt: Long? = null,
    @SerialName("reminder_at") val reminderAt: Long? = null,
    @SerialName("summary_content") val summaryContent: String? = null,
)

@Serializable
data class SupaCategory(
    val id: Int,
    @SerialName("user_id") val userId: String,
    val name: String,
)

@Serializable
data class SupaChatSession(
    val id: Int,
    @SerialName("user_id") val userId: String,
    val title: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long,
    val category: String = "general",
)

@Serializable
data class SupaChatMessage(
    val id: Int,
    @SerialName("user_id") val userId: String,
    @SerialName("session_id") val sessionId: Int,
    val role: String,
    val content: String,
    val timestamp: Long,
    val metadata: String? = null,
)

@Serializable
data class SupaYoutubeSummary(
    @SerialName("user_id") val userId: String,
    @SerialName("video_id") val videoId: String,
    val mode: String,
    val language: String,
    val url: String,
    val summary: String,
    @SerialName("created_at") val createdAt: Long,
)

@Serializable
data class SupaAiUsage(
    val id: Long,
    @SerialName("user_id") val userId: String,
    val feature: String,
    @SerialName("used_at") val usedAt: Long,
)

@Serializable
data class SupaBackupMetadataInsert(
    @SerialName("user_id") val userId: String,
    @SerialName("device_name") val deviceName: String,
    @SerialName("app_version") val appVersion: String,
    @SerialName("db_version") val dbVersion: Int,
    @SerialName("memo_count") val memoCount: Int = 0,
    @SerialName("total_rows") val totalRows: Int = 0,
    @SerialName("size_bytes") val sizeBytes: Long = 0,
)

@Serializable
data class SupaBackupMetadata(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("device_name") val deviceName: String,
    @SerialName("app_version") val appVersion: String,
    @SerialName("db_version") val dbVersion: Int,
    @SerialName("memo_count") val memoCount: Int = 0,
    @SerialName("total_rows") val totalRows: Int = 0,
    @SerialName("size_bytes") val sizeBytes: Long = 0,
    @SerialName("created_at") val createdAt: String = "",
)

// ── Legacy local backup models (JSON export/import) ──

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
