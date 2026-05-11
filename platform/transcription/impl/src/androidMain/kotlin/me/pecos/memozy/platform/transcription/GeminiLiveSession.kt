package me.pecos.memozy.platform.transcription

import android.util.Base64
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.header
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.pecos.memozy.platform.media.RecordingService

/**
 * Gemini Live API WebSocket 세션 — Worker `/gemini-live` proxy 를 거쳐 양방향 audio stream.
 *
 * - 시작: WebSocket open → setup 메시지(model + inputAudioTranscription) 전송 →
 *   [RecordingService] 에 PcmListener 등록.
 * - PCM 청크가 들어올 때마다 base64 인코딩 + realtimeInput 메시지로 송신.
 * - 서버 응답에서 inputTranscription.text 를 추출 → [RecordingService.publishPartial] 로 발행.
 * - 종료: WebSocket close + listener 해제.
 *
 * Worker URL 은 `https://...workers.dev` 형태로 들어오면 `wss://...workers.dev/gemini-live` 로 변환.
 */
internal class GeminiLiveSession(
    private val workerUrl: String,
    private val appKey: String,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    private val httpClient: HttpClient by lazy {
        HttpClient(OkHttp) {
            install(WebSockets)
        }
    }

    fun start(systemPrompt: String) {
        if (job?.isActive == true) return
        val wssUrl = buildWssUrl() + "?k=" + encodeUrlComponent(appKey)
        Log.i(TAG, "starting Gemini Live session → ${buildWssUrl()} (auth via query)")

        val chunkChannel = Channel<ByteArray>(capacity = Channel.UNLIMITED)

        val pcmListener = object : RecordingService.PcmListener {
            override fun onPcmChunk(buf: ByteArray, len: Int) {
                val chunk = if (len == buf.size) buf else buf.copyOf(len)
                chunkChannel.trySend(chunk)
            }
        }

        job = scope.launch {
            try {
                httpClient.webSocket(
                    urlString = wssUrl,
                    request = { header("x-app-key", appKey) },
                ) {
                    val setupJson = buildSetupMessage(systemPrompt)
                    send(Frame.Text(setupJson))
                    Log.i(TAG, "setup sent")

                    RecordingService.addPcmListener(pcmListener)

                    // PCM chunk → WebSocket 송신 코루틴
                    val sendJob = launch {
                        try {
                            for (chunk in chunkChannel) {
                                if (!isActive) break
                                val b64 = Base64.encodeToString(chunk, Base64.NO_WRAP)
                                val msg = """{"realtimeInput":{"audio":{"data":"$b64","mimeType":"audio/pcm;rate=16000"}}}"""
                                try {
                                    send(Frame.Text(msg))
                                } catch (e: Throwable) {
                                    Log.w(TAG, "send failed: ${e.message}")
                                    break
                                }
                            }
                        } catch (e: Throwable) {
                            Log.w(TAG, "send loop error: ${e.message}")
                        }
                    }

                    try {
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                handleServerMessage(frame.readText())
                            }
                        }
                    } finally {
                        sendJob.cancel()
                        chunkChannel.close()
                        RecordingService.removePcmListener(pcmListener)
                    }
                }
            } catch (e: Throwable) {
                Log.w(TAG, "websocket error: ${e.javaClass.simpleName}: ${e.message}", e)
                RecordingService.removePcmListener(pcmListener)
                chunkChannel.close()
            }
        }
    }

    fun stop() {
        Log.i(TAG, "stopping Gemini Live session")
        job?.cancel()
        job = null
    }

    private fun buildWssUrl(): String {
        val base = workerUrl
            .replace("http://", "ws://")
            .replace("https://", "wss://")
            .trimEnd('/')
        return "$base/gemini-live"
    }

    private fun encodeUrlComponent(s: String): String =
        java.net.URLEncoder.encode(s, "UTF-8")

    private fun buildSetupMessage(systemPrompt: String): String {
        // JSON 직접 조립 — Live API 의 가장 기본 setup. 추후 inputAudioTranscription +
        // systemInstruction 활성화 시 systemPrompt 사용.
        val safePrompt = systemPrompt.replace("\\", "\\\\").replace("\"", "\\\"")
        return """
            {"setup":{"model":"models/gemini-3.1-flash-live-preview","generationConfig":{"responseModalities":["TEXT"]},"inputAudioTranscription":{},"systemInstruction":{"parts":[{"text":"$safePrompt"}]}}}
        """.trimIndent()
    }

    /**
     * 단순 정규식 파서 — Live API 의 inputTranscription.text 만 추출.
     * Live API 응답에는 setupComplete, modelTurn, turnComplete 등 다양한 메시지가 섞여있으나
     * 우리는 사용자 발화 텍스트만 필요.
     */
    private fun handleServerMessage(json: String) {
        val text = extractInputTranscription(json)
        if (text != null && text.isNotBlank()) {
            RecordingService.publishPartial(text)
        }
        // turnComplete 등 별도 신호는 현재 무시. partial 누적이 그대로 최종 텍스트.
    }

    private fun extractInputTranscription(json: String): String? {
        // "inputTranscription":{"text":"..."} 형태 매칭
        val regex = "\"inputTranscription\"\\s*:\\s*\\{[^}]*\"text\"\\s*:\\s*\"([^\"]*)\"".toRegex()
        return regex.find(json)?.groupValues?.get(1)
    }

    companion object {
        private const val TAG = "GeminiLiveSession"
    }
}
