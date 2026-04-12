package me.pecos.memozy.data.backup

interface BackupRepository {
    suspend fun createBackupPayload(): BackupPayload
    suspend fun restoreFromPayload(payload: BackupPayload)

    // Cloud operations
    suspend fun uploadBackup(): Result<BackupCreateResponse>
    suspend fun listBackups(): Result<List<BackupMeta>>
    suspend fun downloadBackup(backupId: String): Result<BackupDownloadResponse>
    suspend fun deleteBackup(backupId: String): Result<Unit>
    suspend fun restoreFromCloud(backupId: String): Result<Int>
}
