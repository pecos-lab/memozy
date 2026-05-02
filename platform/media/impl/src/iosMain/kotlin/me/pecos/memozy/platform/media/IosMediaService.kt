package me.pecos.memozy.platform.media

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioPlayerDelegateProtocol
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionDefaultToSpeaker
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVAudioSessionModeDefault
import platform.AVFAudio.AVEncoderAudioQualityKey
import platform.AVFAudio.AVEncoderBitRateKey
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.AVFAudio.setActive
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.darwin.NSObject

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

@OptIn(ExperimentalForeignApi::class)
private class IosAudioRecorder : AudioRecorder {
    private var recorder: AVAudioRecorder? = null

    override fun start(outputPath: String) {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(
            AVAudioSessionCategoryPlayAndRecord,
            mode = AVAudioSessionModeDefault,
            options = AVAudioSessionCategoryOptionDefaultToSpeaker,
            error = null,
        )
        session.setActive(true, error = null)

        val url = NSURL.fileURLWithPath(outputPath)
        val settings = mapOf<Any?, Any>(
            AVFormatIDKey to NSNumber(unsignedInt = kAudioFormatMPEG4AAC),
            AVSampleRateKey to NSNumber(double = 16000.0),
            AVNumberOfChannelsKey to NSNumber(int = 1),
            AVEncoderBitRateKey to NSNumber(int = 64000),
            AVEncoderAudioQualityKey to NSNumber(int = 64), // .medium
        )
        val rec = AVAudioRecorder(uRL = url, settings = settings, error = null)
        rec.prepareToRecord()
        rec.record()
        recorder = rec
    }

    override fun stop() {
        recorder?.stop()
        AVAudioSession.sharedInstance().setActive(false, error = null)
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
