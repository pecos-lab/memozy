package me.pecos.memozy.platform.media

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioPlayerDelegateProtocol
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionDefaultToSpeaker
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVAudioSessionModeDefault
import platform.AVFAudio.AVAudioSessionRecordPermissionDenied
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.AVFAudio.AVEncoderAudioQualityKey
import platform.AVFAudio.AVEncoderBitRateKey
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.AVFAudio.setActive
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.darwin.NSObject
import kotlinx.cinterop.ObjCObjectVar

/**
 * iOS Media — AVFoundation 기반 실 구현.
 * - 녹음: AVAudioRecorder, MPEG4 AAC 16kHz 64kbps (Android 와 일치)
 * - 재생: AVAudioPlayer
 * - AVAudioSession 카테고리는 녹음 시 playAndRecord, 재생 시 playback 으로 전환
 */
class IosMediaService : MediaService {
    override fun createAudioPlayer(sourcePath: String): AudioPlayer = IosAudioPlayer(sourcePath)
    override fun createAudioRecorder(): AudioRecorder = IosAudioRecorder()
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class IosAudioRecorder : AudioRecorder {
    private var recorder: AVAudioRecorder? = null
    private var currentPath: String? = null

    override fun start(outputPath: String) {
        currentPath = outputPath
        println("[IosRecorder] start path=$outputPath")

        val session = AVAudioSession.sharedInstance()
        // 권한 확인 (실 디바이스에서만 의미 있음, 시뮬레이터는 자동 GRANTED)
        val perm = session.recordPermission
        when (perm) {
            AVAudioSessionRecordPermissionDenied -> {
                println("[IosRecorder] mic permission DENIED — Settings 에서 허용 필요")
                return
            }
            AVAudioSessionRecordPermissionGranted -> {
                println("[IosRecorder] mic permission GRANTED")
            }
            else -> {
                // Undetermined — requestRecordPermission 동기 호출 어려우므로 일단 진행 (시스템이 다이얼로그 표시)
                println("[IosRecorder] mic permission UNDETERMINED — 다이얼로그 노출 예정")
                session.requestRecordPermission { granted ->
                    println("[IosRecorder] permission callback granted=$granted")
                }
            }
        }

        memScoped {
            val errSession = alloc<ObjCObjectVar<NSError?>>()
            val ok1 = session.setCategory(
                AVAudioSessionCategoryPlayAndRecord,
                mode = AVAudioSessionModeDefault,
                options = AVAudioSessionCategoryOptionDefaultToSpeaker,
                error = errSession.ptr,
            )
            if (!ok1 || errSession.value != null) {
                println("[IosRecorder] setCategory FAILED: ${errSession.value?.localizedDescription}")
                return
            }
            val errActive = alloc<ObjCObjectVar<NSError?>>()
            val ok2 = session.setActive(true, error = errActive.ptr)
            if (!ok2 || errActive.value != null) {
                println("[IosRecorder] setActive FAILED: ${errActive.value?.localizedDescription}")
                return
            }
        }

        val url = NSURL.fileURLWithPath(outputPath)
        val settings = mapOf<Any?, Any>(
            AVFormatIDKey to NSNumber(unsignedInt = kAudioFormatMPEG4AAC),
            AVSampleRateKey to NSNumber(double = 16000.0),
            AVNumberOfChannelsKey to NSNumber(int = 1),
            AVEncoderBitRateKey to NSNumber(int = 64000),
            AVEncoderAudioQualityKey to NSNumber(int = 64),
        )

        memScoped {
            val errInit = alloc<ObjCObjectVar<NSError?>>()
            val rec = AVAudioRecorder(uRL = url, settings = settings, error = errInit.ptr)
            if (errInit.value != null) {
                println("[IosRecorder] init FAILED: ${errInit.value?.localizedDescription}")
                return
            }
            val prepared = rec.prepareToRecord()
            println("[IosRecorder] prepareToRecord = $prepared")
            if (!prepared) return
            val started = rec.record()
            println("[IosRecorder] record() returned = $started")
            if (!started) return
            recorder = rec
        }
    }

    override fun stop() {
        val rec = recorder
        rec?.stop()
        recorder = null
        memScoped {
            val err = alloc<ObjCObjectVar<NSError?>>()
            AVAudioSession.sharedInstance().setActive(false, error = err.ptr)
            if (err.value != null) {
                println("[IosRecorder] setActive(false) error: ${err.value?.localizedDescription}")
            }
        }
        val path = currentPath
        if (path != null) {
            val exists = NSFileManager.defaultManager.fileExistsAtPath(path)
            val attrs = NSFileManager.defaultManager.attributesOfItemAtPath(path, error = null)
            val size = (attrs?.get(platform.Foundation.NSFileSize) as? Number)?.toLong() ?: 0L
            println("[IosRecorder] stop — file exists=$exists, size=$size bytes")
        }
    }

    override fun release() {
        recorder = null
    }
}

@OptIn(ExperimentalForeignApi::class)
private class IosAudioPlayer(sourcePath: String) : AudioPlayer {
    private val player: AVAudioPlayer?
    private var completionDelegate: PlayerDelegate? = null

    init {
        AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, error = null)
        AVAudioSession.sharedInstance().setActive(true, error = null)
        val url = NSURL.fileURLWithPath(sourcePath)
        player = AVAudioPlayer(contentsOfURL = url, error = null)
        player?.prepareToPlay()
    }

    override fun start() {
        player?.play()
    }

    override fun pause() {
        player?.pause()
    }

    override fun release() {
        player?.stop()
        completionDelegate = null
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        val delegate = PlayerDelegate(listener)
        completionDelegate = delegate
        player?.setDelegate(delegate)
    }
}

private class PlayerDelegate(
    private val onComplete: () -> Unit,
) : NSObject(), AVAudioPlayerDelegateProtocol {
    override fun audioPlayerDidFinishPlaying(player: AVAudioPlayer, successfully: Boolean) {
        onComplete()
    }
}
