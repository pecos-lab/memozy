package me.pecos.memozy.platform.media

import android.content.Context
import android.media.MediaPlayer

/**
 * Android Media — 재생 전용.
 * - 재생: MediaPlayer
 * - 녹음/STT 는 LiveTranscriptionService (RecordingService) 가 마이크 단독 소유 + AAC 파일 + Live STT 통합 담당.
 *   iOS 와 동일한 구조 — AudioRecorder 호출은 NoopAudioRecorder 로 차단.
 */
class AndroidMediaService(
    private val context: Context,
) : MediaService {
    override fun createAudioPlayer(sourcePath: String): AudioPlayer =
        AndroidAudioPlayer(sourcePath)

    override fun createAudioRecorder(): AudioRecorder = NoopAudioRecorder
}

private object NoopAudioRecorder : AudioRecorder {
    override fun start(outputPath: String) { /* LiveTranscriptionService 가 마이크 사용 — no-op */ }
    override fun stop() {}
    override fun release() {}
}

private class AndroidAudioPlayer(sourcePath: String) : AudioPlayer {
    private val player = MediaPlayer().apply {
        setDataSource(sourcePath)
        prepare()
    }

    override fun start() {
        player.start()
    }

    override fun pause() {
        player.pause()
    }

    override fun release() {
        player.release()
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        player.setOnCompletionListener { listener() }
    }
}
