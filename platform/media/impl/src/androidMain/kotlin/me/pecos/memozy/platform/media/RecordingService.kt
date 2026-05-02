package me.pecos.memozy.platform.media

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 녹음용 Foreground Service.
 *
 * 화면이 꺼지거나 앱이 백그라운드로 가도 녹음이 끊기지 않도록 MediaRecorder 를 별도 서비스에서 보유.
 * 알림바에 녹음 상태 + 정지 버튼 노출 (Android 13+ 의 `microphone` foregroundServiceType 사용).
 */
internal class RecordingService : Service() {

    private var recorder: MediaRecorder? = null
    private var outputPath: String? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val path = intent.getStringExtra(EXTRA_OUTPUT_PATH)
                if (path != null) {
                    startRecording(path)
                }
            }
            ACTION_STOP -> {
                if (recorder == null) {
                    android.util.Log.i(TAG, "STOP received but recorder is null — already stopped or never started")
                }
                stopRecordingAndService()
            }
        }
        // START_NOT_STICKY: 프로세스 종료 시 Service 자동 재시작 안 함.
        // 이유: companion object 의 _state StateFlow 는 재시작 후 부정확하므로 상태 일관성 유지를 위해 명시적 재시작만 허용.
        return START_NOT_STICKY
    }

    private fun startRecording(path: String) {
        if (recorder != null) return // 이미 녹음 중
        outputPath = path

        startForegroundCompat()

        recorder = createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(16000)
            setAudioEncodingBitRate(64000)
            setOutputFile(path)
            try {
                prepare()
                start()
                _state.value = RecordingState.Recording(path)
            } catch (e: Exception) {
                _state.value = RecordingState.Idle
                stopRecordingAndService()
            }
        }
    }

    private fun stopRecordingAndService() {
        try {
            recorder?.apply {
                try { stop() } catch (_: Throwable) {}
                release()
            }
        } finally {
            recorder = null
            _state.value = RecordingState.Idle
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
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
            // 채널이 없을 때만 생성 — 매번 createNotificationChannel 호출 비용 회피
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

    @Suppress("DEPRECATION")
    private fun createRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()
        }

    override fun onDestroy() {
        super.onDestroy()
        stopRecordingAndService()
    }

    companion object {
        const val ACTION_START = "me.pecos.memozy.recording.START"
        const val ACTION_STOP = "me.pecos.memozy.recording.STOP"
        const val EXTRA_OUTPUT_PATH = "output_path"

        private const val CHANNEL_ID = "memozy.recording"
        private const val NOTIFICATION_ID = 8801
        private const val REQUEST_STOP = 1
        private const val TAG = "RecordingService"

        private val _state = MutableStateFlow<RecordingState>(RecordingState.Idle)
        val state: StateFlow<RecordingState> = _state

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

internal sealed class RecordingState {
    data object Idle : RecordingState()
    data class Recording(val outputPath: String) : RecordingState()
}
