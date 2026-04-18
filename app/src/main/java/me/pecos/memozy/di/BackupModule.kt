package me.pecos.memozy.di

import android.content.Context
import android.os.Build
import android.provider.Settings
import me.pecos.memozy.data.backup.BackupRepository
import me.pecos.memozy.data.backup.BackupRepositoryImpl
import me.pecos.memozy.data.datasource.local.MEMO_DB_VERSION
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val backupModule = module {
    single<BackupRepository> {
        val context: Context = androidContext()
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val deviceName = Settings.Global.getString(context.contentResolver, "device_name")
            ?: Build.MODEL
        val appVersion = packageInfo.versionName ?: "unknown"
        BackupRepositoryImpl(
            memoDao = get(),
            categoryDao = get(),
            chatSessionDao = get(),
            chatMessageDao = get(),
            youtubeSummaryDao = get(),
            aiUsageDao = get(),
            authService = get(),
            supabaseClient = get(),
            deviceName = deviceName,
            appVersion = appVersion,
            dbVersion = MEMO_DB_VERSION,
        )
    }
}
