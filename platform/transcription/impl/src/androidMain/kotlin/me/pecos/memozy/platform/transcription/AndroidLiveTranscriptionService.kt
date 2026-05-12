package me.pecos.memozy.platform.transcription

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

fun provideLiveTranscriptionService(context: Context): LiveTranscriptionService =
    AndroidLiveTranscriptionService(context.applicationContext)

/**
 * iOS LiveTranscriptionBridge 와 동일한 책임: 마이크 + 실시간 STT + 파일 저장 통합.
 *
 * 실제 작업은 [RecordingService] 가 담당 (AudioRecord → SpeechRecognizer pipe + AAC 인코더).
 * 이 클래스는 commonMain interface 를 RecordingService 의 companion StateFlow 에 연결하는 얇은 wrapper.
 */
internal class AndroidLiveTranscriptionService(
    private val context: Context,
) : LiveTranscriptionService {

    override val partialText: StateFlow<String> = RecordingService.partialText
    override val confirmedText: StateFlow<String> = RecordingService.confirmedText

    private val _state = MutableStateFlow<TranscriptionState>(TranscriptionState.Idle)
    override val state: StateFlow<TranscriptionState> = _state

    override suspend fun start(languageCode: String, outputPath: String?) {
        if (outputPath == null) {
            // outputPath 없이는 RecordingService 가 안 돌아감 — caller 가 반드시 경로 제공해야 함.
            _state.value = TranscriptionState.Error("outputPath required")
            return
        }
        RecordingService.resetLiveText()
        RecordingService.start(context, outputPath, languageCode)
        _state.value = TranscriptionState.Listening
    }

    override fun stop() {
        RecordingService.stop(context)
        _state.value = TranscriptionState.Idle
    }
}
