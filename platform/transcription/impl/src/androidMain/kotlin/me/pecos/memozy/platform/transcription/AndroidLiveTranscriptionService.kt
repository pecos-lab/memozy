package me.pecos.memozy.platform.transcription

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.pecos.memozy.platform.media.RecordingService
import me.pecos.memozy.platform.media.RecordingState

fun provideLiveTranscriptionService(
    context: Context,
    workerUrl: String,
    appKey: String,
): LiveTranscriptionService =
    AndroidLiveTranscriptionService(context.applicationContext, workerUrl, appKey)

/**
 * iOS 의 SFSpeechRecognizer + AVAudioEngine 패턴을 Android 에 맞춰 구현.
 *
 * 실제 음성 캡처는 [RecordingService] 가 mic 1개 클라이언트(AudioRecord)로 통합 수행.
 * 실시간 STT 는 [GeminiLiveSession] 이 PCM 청크를 WebSocket 으로 Gemini Live API 에 stream.
 *
 * 결과(partial/confirmed) 는 RecordingService 의 companion StateFlow 를 그대로 노출.
 */
internal class AndroidLiveTranscriptionService(
    @Suppress("UNUSED_PARAMETER") private val context: Context,
    private val workerUrl: String,
    private val appKey: String,
) : LiveTranscriptionService {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var session: GeminiLiveSession? = null

    override val partialText: StateFlow<String> = RecordingService.partial
    override val confirmedText: StateFlow<String> = RecordingService.confirmed

    private val _state = MutableStateFlow<TranscriptionState>(TranscriptionState.Idle)
    override val state: StateFlow<TranscriptionState> = _state

    init {
        scope.launch {
            RecordingService.state.collect { rec ->
                _state.value = when (rec) {
                    is RecordingState.Idle -> TranscriptionState.Idle
                    is RecordingState.Recording -> TranscriptionState.Listening
                }
            }
        }
    }

    override suspend fun start(languageCode: String, outputPath: String?) {
        if (workerUrl.isBlank() || appKey.isBlank()) {
            _state.value = TranscriptionState.Error("Worker URL / app key 미설정")
            return
        }
        // 새 세션 — 기존 세션이 남아있으면 정리
        session?.stop()
        val prompt = buildSystemPrompt(languageCode)
        session = GeminiLiveSession(workerUrl, appKey).apply { start(prompt) }
    }

    override fun stop() {
        session?.stop()
        session = null
    }

    private fun buildSystemPrompt(languageCode: String): String {
        val langName = when (languageCode) {
            "ko" -> "한국어"
            "en" -> "영어"
            "ja" -> "일본어"
            else -> languageCode
        }
        return "사용자가 발화한 $langName 음성을 정확히 받아쓰기만 하세요. 별도 응답이나 추가 텍스트는 출력하지 마세요."
    }
}
