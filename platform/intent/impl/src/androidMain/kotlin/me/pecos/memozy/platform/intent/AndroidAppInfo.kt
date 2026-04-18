package me.pecos.memozy.platform.intent

import android.content.Context
import android.os.Build

class AndroidAppInfo(
    private val context: Context,
) : AppInfo {
    override val packageName: String = context.packageName

    override val versionName: String by lazy {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.PackageInfoFlags.of(0L),
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }.versionName
        }.getOrNull().orEmpty()
    }
}
