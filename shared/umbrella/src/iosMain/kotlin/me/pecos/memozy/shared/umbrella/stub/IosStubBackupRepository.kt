package me.pecos.memozy.shared.umbrella.stub

import me.pecos.memozy.data.backup.BackupPayload
import me.pecos.memozy.data.backup.BackupRepository
import me.pecos.memozy.data.backup.BackupTables

/**
 * iOS 용 임시 BackupRepository 스텁. Supabase 연결 완료 전까지 클라우드 동작은
 * 항상 실패를 반환하고, 로컬 JSON 백업은 빈 페이로드로 응답한다.
 */
class IosStubBackupRepository : BackupRepository {
    override suspend fun uploadBackup(): Result<Int> =
        Result.failure(UnsupportedOperationException("Cloud backup not wired on iOS yet"))

    override suspend fun restoreFromCloud(): Result<Int> =
        Result.failure(UnsupportedOperationException("Cloud backup not wired on iOS yet"))

    override suspend fun getLastBackupTime(): Result<String?> = Result.success(null)

    override suspend fun createBackupPayload(): BackupPayload = BackupPayload(
        deviceName = "iOS",
        appVersion = "",
        dbVersion = 0,
        tables = BackupTables(memos = emptyList(), categories = emptyList()),
    )

    override suspend fun restoreFromPayload(payload: BackupPayload) = Unit
}
