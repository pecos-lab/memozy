package me.pecos.memozy.platform.transcription

import kotlinx.coroutines.flow.StateFlow

/**
 * 실시간 받아쓰기 서비스 (속기록 스타일).
 *
 * - [partialText]: 현재 발화 중인 미확정 텍스트 (회색으로 표시 권장)
 * - [confirmedText]: 발화 단락이 끝나서 확정된 누적 텍스트
 *
 * 한 번 [start] 호출 후 [stop] 호출까지 사용자 발화 흐름 유지 책임은 구현체가 갖는다.
 * (iOS SFSpeechRecognizer 의 ~1분 세션 한도, Android SpeechRecognizer 의 무음 timeout 등은
 * 구현체가 자동으로 세션 재시작으로 처리해야 함.)
 */
interface LiveTranscriptionService {
    val partialText: StateFlow<String>
    val confirmedText: StateFlow<String>
    val state: StateFlow<TranscriptionState>

    /**
     * @param languageCode 인식 언어 (ko/en/ja)
     * @param outputPath 인식과 동시에 audio 를 WAV 로 캡처할 경로 (null 이면 인식만)
     *                   iOS 에서는 AVAudioEngine tap 으로 캡처. Android 는 무시 (별도 MediaRecorder 사용).
     */
    suspend fun start(languageCode: String = "ko", outputPath: String? = null)
    fun stop()
}

sealed class TranscriptionState {
    data object Idle : TranscriptionState()
    data object Listening : TranscriptionState()
    data class Error(val message: String) : TranscriptionState()
}
