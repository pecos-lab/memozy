package me.pecos.memozy.platform.media

interface MediaService {
    fun createAudioPlayer(sourcePath: String): AudioPlayer
    fun createAudioRecorder(): AudioRecorder
}

interface AudioPlayer {
    fun start()
    fun pause()
    fun release()
    fun setOnCompletionListener(listener: () -> Unit)
}

interface AudioRecorder {
    fun start(outputPath: String)
    fun stop()
    fun release()
}
