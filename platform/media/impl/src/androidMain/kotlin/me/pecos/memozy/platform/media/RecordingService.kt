package me.pecos.memozy.platform.media

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * 녹음 + 실시간 받아쓰기 통합 Foreground Service.
 *
 * iOS 의 AVAudioEngine input tap 패턴을 흉내 — mic 1개 클라이언트(AudioRecord)에서 PCM 을 받아
 * (1) WAV 파일에 그대로 쓰기 + (2) 등록된 PcmListener 들에 broadcast.
 *
 * 기존 MediaRecorder + SpeechRecognizer 동시 사용 시 mic 자원 경합으로
 * SpeechRecognizer 가 NO_SPEECH_DETECTED 만 뱉던 회귀를 차단.
 *
 * 실시간 STT 는 외부 구현 (GeminiLiveTranscriptionService 등) 이 PcmListener 로 PCM 을
 * 받아 자체적으로 처리하고, [publishPartial] / [publishConfirmed] 로 결과를 push 한다.
 */
class RecordingService : Service() {

    private val running = AtomicBoolean(false)
    private var audioRecord: AudioRecord? = null
    private var captureThread: Thread? = null
    private var outputPath: String? = null
    private var wavSink: WavFileSink? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val path = intent.getStringExtra(EXTRA_OUTPUT_PATH)
                if (path != null && !running.get()) {
                    startCapture(path)
                }
            }
            ACTION_STOP -> stopCapture()
        }
        return START_NOT_STICKY
    }

    private fun startCapture(path: String) {
        outputPath = path
        _partial.value = ""
        _confirmed.value = ""

        startForegroundCompat()

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "RECORD_AUDIO permission not granted")
            _state.value = RecordingState.Idle
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        val sink = WavFileSink(path, SAMPLE_RATE, 1, 16).also { it.open() }
        wavSink = sink

        val minBuf = AudioRecord.getMinBufferSize(
            SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBuf <= 0) {
            Log.e(TAG, "AudioRecord.getMinBufferSize failed ($minBuf)")
            stopCapture()
            return
        }
        val bufSize = minBuf * 2

        val rec = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufSize,
            )
        } catch (e: Throwable) {
            Log.e(TAG, "AudioRecord ctor failed", e)
            stopCapture()
            return
        }

        if (rec.state != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord not initialized (state=${rec.state})")
            try { rec.release() } catch (_: Throwable) {}
            stopCapture()
            return
        }

        try {
            rec.startRecording()
        } catch (e: Throwable) {
            Log.e(TAG, "AudioRecord startRecording failed", e)
            try { rec.release() } catch (_: Throwable) {}
            stopCapture()
            return
        }

        audioRecord = rec
        running.set(true)
        _state.value = RecordingState.Recording(path)

        captureThread = thread(name = "MemozyPcmCapture", isDaemon = false) {
            val buffer = ByteArray(bufSize)
            while (running.get()) {
                val n = try {
                    rec.read(buffer, 0, buffer.size)
                } catch (e: Throwable) {
                    Log.w(TAG, "AudioRecord.read threw: ${e.message}")
                    break
                }
                if (n > 0) {
                    try { sink.write(buffer, n) } catch (e: Throwable) {
                        Log.w(TAG, "WAV write failed: ${e.message}")
                    }
                    // Listener broadcast — listener 측에서 자체적으로 throttle/network IO 분리
                    val listeners = pcmListeners
                    if (listeners.isNotEmpty()) {
                        // copy buffer slice to avoid mutation race
                        val chunk = ByteArray(n)
                        System.arraycopy(buffer, 0, chunk, 0, n)
                        for (l in listeners) {
                            try { l.onPcmChunk(chunk, n) } catch (e: Throwable) {
                                Log.w(TAG, "pcm listener failed: ${e.message}")
                            }
                        }
                    }
                } else if (n < 0) {
                    Log.w(TAG, "AudioRecord.read returned $n")
                    break
                }
            }
        }
    }

    private fun stopCapture() {
        if (!running.compareAndSet(true, false)) {
            _state.value = RecordingState.Idle
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        try { captureThread?.join(800) } catch (_: Throwable) {}
        captureThread = null

        audioRecord?.let { rec ->
            try { rec.stop() } catch (_: Throwable) {}
            try { rec.release() } catch (_: Throwable) {}
        }
        audioRecord = null

        // partial 잔여 → confirmed 로 흡수
        val tail = _partial.value
        if (tail.isNotBlank()) {
            val cur = _confirmed.value
            _confirmed.value = if (cur.isEmpty()) tail else "$cur $tail"
            _partial.value = ""
        }

        wavSink?.close()
        wavSink = null

        _state.value = RecordingState.Idle
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startForegroundCompat() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(): Notification {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "녹음",
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = "메모 음성 녹음 진행 상태"
                    setSound(null, null)
                    enableVibration(false)
                }
                nm.createNotificationChannel(channel)
            }
        }

        val stopIntent = Intent(this, RecordingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this,
            REQUEST_STOP,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Memozy 녹음 중")
            .setContentText("탭하여 정지")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_media_pause,
                "정지",
                stopPending,
            )
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (running.get()) stopCapture()
    }

    interface PcmListener {
        /** 16kHz mono 16-bit PCM little-endian. buf[0..len) 만 유효. buf 는 호출자가 소유. */
        fun onPcmChunk(buf: ByteArray, len: Int)
    }

    companion object {
        const val ACTION_START = "me.pecos.memozy.recording.START"
        const val ACTION_STOP = "me.pecos.memozy.recording.STOP"
        const val EXTRA_OUTPUT_PATH = "output_path"
        const val SAMPLE_RATE = 16000

        private const val CHANNEL_ID = "memozy.recording"
        private const val NOTIFICATION_ID = 8801
        private const val REQUEST_STOP = 1
        private const val TAG = "RecordingService"

        private val _state = MutableStateFlow<RecordingState>(RecordingState.Idle)
        val state: StateFlow<RecordingState> = _state

        private val _partial = MutableStateFlow("")
        private val _confirmed = MutableStateFlow("")
        val partial: StateFlow<String> = _partial
        val confirmed: StateFlow<String> = _confirmed

        private val pcmListeners = CopyOnWriteArrayList<PcmListener>()

        fun addPcmListener(l: PcmListener) { pcmListeners.addIfAbsent(l) }
        fun removePcmListener(l: PcmListener) { pcmListeners.remove(l) }

        /** 외부 STT 구현이 partial 결과를 발행. */
        fun publishPartial(text: String) {
            _partial.value = text
        }

        /** 외부 STT 구현이 확정 결과(문장 단위)를 누적 발행. */
        fun publishConfirmed(text: String) {
            if (text.isBlank()) return
            val cur = _confirmed.value
            _confirmed.value = if (cur.isEmpty()) text else "$cur $text"
            _partial.value = ""
        }

        fun start(context: Context, outputPath: String) {
            val intent = Intent(context, RecordingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_OUTPUT_PATH, outputPath)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, RecordingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}

sealed class RecordingState {
    data object Idle : RecordingState()
    data class Recording(val outputPath: String) : RecordingState()
}
