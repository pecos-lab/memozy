package me.pecos.memozy.platform.intent

enum class AppPermission { RECORD_AUDIO }

enum class PermissionStatus { GRANTED, DENIED, UNKNOWN }

interface PermissionService {
    fun status(permission: AppPermission): PermissionStatus
}
