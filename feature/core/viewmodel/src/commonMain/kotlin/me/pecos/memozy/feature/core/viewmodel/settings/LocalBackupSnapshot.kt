package me.pecos.memozy.feature.core.viewmodel.settings

import kotlinx.serialization.Serializable

@Serializable
internal data class LocalBackupSnapshot(
    val version: Int = 1,
    val appVersion: String = "memozy",
    val exportedAt: Long,
    val memos: List<LocalBackupMemo>,
)

@Serializable
internal data class LocalBackupMemo(
    val id: Int,
    val name: String,
    val categoryId: Int = 1,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val format: String = "PLAIN",
    val isPinned: Boolean = false,
    val audioPath: String? = null,
    val styles: String? = null,
    val youtubeUrl: String? = null,
    val deletedAt: Long? = null,
    val summaryContent: String? = null,
)
