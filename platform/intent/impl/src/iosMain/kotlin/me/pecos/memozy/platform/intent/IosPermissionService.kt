package me.pecos.memozy.platform.intent

import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionDenied
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted

class IosPermissionService : PermissionService {
    override fun status(permission: AppPermission): PermissionStatus = when (permission) {
        AppPermission.RECORD_AUDIO -> when (AVAudioSession.sharedInstance().recordPermission) {
            AVAudioSessionRecordPermissionGranted -> PermissionStatus.GRANTED
            AVAudioSessionRecordPermissionDenied -> PermissionStatus.DENIED
            else -> PermissionStatus.UNKNOWN
        }
    }
}
