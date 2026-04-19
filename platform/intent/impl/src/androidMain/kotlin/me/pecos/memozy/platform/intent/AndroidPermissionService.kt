package me.pecos.memozy.platform.intent

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class AndroidPermissionService(
    private val context: Context,
) : PermissionService {
    override fun status(permission: AppPermission): PermissionStatus {
        val name = when (permission) {
            AppPermission.RECORD_AUDIO -> Manifest.permission.RECORD_AUDIO
        }
        val result = ContextCompat.checkSelfPermission(context, name)
        return if (result == PackageManager.PERMISSION_GRANTED) {
            PermissionStatus.GRANTED
        } else {
            PermissionStatus.DENIED
        }
    }
}
