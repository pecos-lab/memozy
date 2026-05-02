package me.pecos.memozy.platform.media

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build

class AndroidMediaService(
    private val context: Context,
) : MediaService {
    override fun createAudioPlayer(sourcePath: String): AudioPlayer =
        AndroidAudioPlayer(sourcePath)

    override fun createAudioRecorder(): AudioRecorder =
        AndroidAudioRecorder(context)
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

/**
 * MediaRecorder 를 직접 들고있던 기존 구조 → RecordingService 위임으로 전환.
 * 화면 꺼짐/백그라운드에서도 녹음 유지. start/stop 은 Service Intent 로 전달.
 */
private class AndroidAudioRecorder(private val context: Context) : AudioRecorder {
    override fun start(outputPath: String) {
        RecordingService.start(context, outputPath)
    }

    override fun stop() {
        RecordingService.stop(context)
    }

    override fun release() {
        // Service 가 stop 시점에 내부 MediaRecorder 를 release. 별도 작업 불필요.
    }
}
