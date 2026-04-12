package me.pecos.memozy.data.backup

interface BackupRepository {
    suspend fun createBackupPayload(): BackupPayload
    suspend fun restoreFromPayload(payload: BackupPayload)
}
