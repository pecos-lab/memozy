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

private class AndroidAudioRecorder(private val context: Context) : AudioRecorder {
    private var recorder: MediaRecorder? = null

    override fun start(outputPath: String) {
        val rec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        rec.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(16000)
            setAudioEncodingBitRate(64000)
            setOutputFile(outputPath)
            prepare()
            start()
        }
        recorder = rec
    }

    override fun stop() {
        recorder?.stop()
    }

    override fun release() {
        recorder?.release()
        recorder = null
    }
}
