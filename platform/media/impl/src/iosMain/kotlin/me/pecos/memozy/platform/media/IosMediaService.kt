package me.pecos.memozy.platform.media

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioPlayerDelegateProtocol
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSURL
import platform.darwin.NSObject

/**
 * iOS Media — 재생 전용 실구현.
 * - 재생: AVAudioPlayer
 * - 녹음: LiveTranscription 의 AVAudioEngine.inputNode tap 으로 통합 캡처되므로 별도 recorder 미사용.
 *   AVAudioRecorder 동시 사용 시 mic 점유 충돌이라 NoopAudioRecorder 로 차단.
 */
class IosMediaService : MediaService {
    override fun createAudioPlayer(sourcePath: String): AudioPlayer = IosAudioPlayer(sourcePath)
    override fun createAudioRecorder(): AudioRecorder = NoopAudioRecorder
}

private object NoopAudioRecorder : AudioRecorder {
    override fun start(outputPath: String) { /* LiveSTT 가 mic 사용 — no-op */ }
    override fun stop() {}
    override fun release() {}
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
