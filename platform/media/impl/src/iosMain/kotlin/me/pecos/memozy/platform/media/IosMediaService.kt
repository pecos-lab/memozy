package me.pecos.memozy.platform.media

/**
 * iOS no-op stub.
 *
 * WHY: AVAudioPlayer / AVAudioRecorder 통합은 별도 PR (Wave 3 후속). 이 stub 은
 * Koin DI 시점의 누락으로 MemoPlain 화면 자체가 못 뜨는 문제를 막기 위함.
 * 오디오 재생/녹음은 호출돼도 동작하지 않으며 (재생 진행 X, 녹음 파일 생성 X),
 * 메모 CRUD/네비 등 비-오디오 흐름만 정상 동작한다.
 */
class IosMediaService : MediaService {
    override fun createAudioPlayer(sourcePath: String): AudioPlayer = NoopAudioPlayer
    override fun createAudioRecorder(): AudioRecorder = NoopAudioRecorder
}

private object NoopAudioPlayer : AudioPlayer {
    override fun start() {}
    override fun pause() {}
    override fun release() {}
    override fun setOnCompletionListener(listener: () -> Unit) {}
}

private object NoopAudioRecorder : AudioRecorder {
    override fun start(outputPath: String) {}
    override fun stop() {}
    override fun release() {}
}
