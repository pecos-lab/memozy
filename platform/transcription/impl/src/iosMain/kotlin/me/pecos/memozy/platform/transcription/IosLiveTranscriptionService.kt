package me.pecos.memozy.platform.transcription

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Swift 브릿지로 위임 — SFSpeechRecognizer + AVAudioEngine 기반 실시간 받아쓰기.
 * 60초 세션 한도는 Swift 측에서 자동 재시작으로 처리.
 */
interface LiveTranscriptionBridge {
    fun start(languageCode: String)
    fun stop()
}

object LiveTranscriptionRegistrar {
    var bridge: LiveTranscriptionBridge? = null

    /**
     * Swift 가 인식 결과를 push 할 때 호출. Kotlin 의 StateFlow 갱신.
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

    fun onState(state: TranscriptionState) {
        IosLiveTranscriptionState._state.value = state
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

    override suspend fun start(languageCode: String) {
        IosLiveTranscriptionState._partial.value = ""
        IosLiveTranscriptionState._confirmed.value = ""
        LiveTranscriptionRegistrar.bridge?.start(languageCode)
            ?: run { IosLiveTranscriptionState._state.value = TranscriptionState.Error("Bridge not registered") }
    }

    override fun stop() {
        LiveTranscriptionRegistrar.bridge?.stop()
    }
}
