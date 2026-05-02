package me.pecos.memozy.platform.transcription

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

fun provideLiveTranscriptionService(context: Context): LiveTranscriptionService =
    AndroidLiveTranscriptionService(context.applicationContext)

internal class AndroidLiveTranscriptionService(
    private val context: Context,
) : LiveTranscriptionService {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var languageCode: String = "ko"
    private var stillListening = false

    private val _partial = MutableStateFlow("")
    private val _confirmed = MutableStateFlow("")
    private val _state = MutableStateFlow<TranscriptionState>(TranscriptionState.Idle)

    override val partialText: StateFlow<String> = _partial
    override val confirmedText: StateFlow<String> = _confirmed
    override val state: StateFlow<TranscriptionState> = _state

    override suspend fun start(languageCode: String) {
        this.languageCode = languageCode
        _partial.value = ""
        _confirmed.value = ""
        stillListening = true
        mainHandler.post { startSession() }
    }

    override fun stop() {
        stillListening = false
        mainHandler.post {
            recognizer?.stopListening()
            recognizer?.destroy()
            recognizer = null
            // partial 에 남아있는 텍스트도 confirmed 로 이관
            if (_partial.value.isNotEmpty()) {
                _confirmed.value = (_confirmed.value + " " + _partial.value).trim()
                _partial.value = ""
            }
            _state.value = TranscriptionState.Idle
        }
    }

    private fun startSession() {
        recognizer?.destroy()
        val rec = SpeechRecognizer.createSpeechRecognizer(context)
        rec.setRecognitionListener(buildListener())
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, mapLocale(languageCode))
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
        }
        rec.startListening(intent)
        recognizer = rec
        _state.value = TranscriptionState.Listening
    }

    private fun buildListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}

        override fun onPartialResults(partialResults: Bundle?) {
            val text = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull().orEmpty()
            if (text.isNotEmpty()) _partial.value = text
        }

        override fun onResults(results: Bundle?) {
            val text = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull().orEmpty()
            if (text.isNotEmpty()) {
                _confirmed.value = (_confirmed.value + " " + text).trim()
                _partial.value = ""
            }
            // 사용자가 아직 정지 안 했으면 자동 재시작
            if (stillListening) {
                mainHandler.post { startSession() }
            }
        }

        override fun onError(error: Int) {
            // 음성 무음 / 네트워크 등 — 단순히 재시작 시도
            if (stillListening && error != SpeechRecognizer.ERROR_CLIENT) {
                mainHandler.postDelayed({ if (stillListening) startSession() }, 100)
            } else {
                _state.value = TranscriptionState.Error("error_code=$error")
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun mapLocale(code: String): String = when (code) {
        "ko" -> "ko-KR"
        "en" -> "en-US"
        "ja" -> "ja-JP"
        else -> code
    }
}
