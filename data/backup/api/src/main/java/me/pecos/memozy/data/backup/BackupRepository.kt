package me.pecos.memozy.data.backup

interface BackupRepository {
    // Cloud operations (Supabase direct upsert)
    suspend fun uploadBackup(): Result<Int>
    suspend fun restoreFromCloud(): Result<Int>
    suspend fun getLastBackupTime(): Result<String?>

    // Local JSON backup (legacy)
    suspend fun createBackupPayload(): BackupPayload
    suspend fun restoreFromPayload(payload: BackupPayload)
}
