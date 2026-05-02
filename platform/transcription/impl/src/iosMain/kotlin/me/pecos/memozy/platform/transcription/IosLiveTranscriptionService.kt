package me.pecos.memozy.platform.transcription

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Swift 브릿지로 위임 — SFSpeechRecognizer + AVAudioEngine 기반 실시간 받아쓰기.
 * 60초 세션 한도는 Swift 측에서 자동 재시작으로 처리.
 */
interface LiveTranscriptionBridge {
    /**
     * @param outputPath null 이면 인식만, 경로 주어지면 AVAudioEngine input tap 으로 WAV 도 동시 캡처
     */
    fun start(languageCode: String, outputPath: String?)
    fun stop()
}

object LiveTranscriptionRegistrar {
    var bridge: LiveTranscriptionBridge? = null

    /**
     * Swift 가 인식 결과를 push 할 때 호출. Kotlin 의 StateFlow 갱신.
     * sealed class 인스턴스 생성을 Swift 에 노출하지 않기 위해 편의 함수로 래핑.
     */
    fun onPartial(text: String) {
        IosLiveTranscriptionState._partial.value = text
    }

    fun onConfirmed(appendText: String) {
        val current = IosLiveTranscriptionState._confirmed.value
        val joined = if (current.isEmpty()) appendText else "$current $appendText"
        IosLiveTranscriptionState._confirmed.value = joined.trim()
        IosLiveTranscriptionState._partial.value = ""
    }

    fun onIdle() {
        IosLiveTranscriptionState._state.value = TranscriptionState.Idle
    }

    fun onListening() {
        IosLiveTranscriptionState._state.value = TranscriptionState.Listening
    }

    fun onError(message: String) {
        IosLiveTranscriptionState._state.value = TranscriptionState.Error(message)
    }
}

internal object IosLiveTranscriptionState {
    val _partial = MutableStateFlow("")
    val _confirmed = MutableStateFlow("")
    val _state = MutableStateFlow<TranscriptionState>(TranscriptionState.Idle)
}

class IosLiveTranscriptionService : LiveTranscriptionService {
    override val partialText: StateFlow<String> = IosLiveTranscriptionState._partial
    override val confirmedText: StateFlow<String> = IosLiveTranscriptionState._confirmed
    override val state: StateFlow<TranscriptionState> = IosLiveTranscriptionState._state

    override suspend fun start(languageCode: String, outputPath: String?) {
        IosLiveTranscriptionState._partial.value = ""
        IosLiveTranscriptionState._confirmed.value = ""
        val b = LiveTranscriptionRegistrar.bridge
        if (b == null) {
            IosLiveTranscriptionState._state.value = TranscriptionState.Error("Bridge not registered")
        } else {
            b.start(languageCode, outputPath)
        }
    }

    override fun stop() {
        LiveTranscriptionRegistrar.bridge?.stop()
    }
}
