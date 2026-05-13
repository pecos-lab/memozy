package me.pecos.memozy.platform.transcription

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.OutputStream
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 통합 녹음 + Live STT Foreground Service.
 *
 * iOS AVAudioEngine 패턴:
 *   AudioRecord (마이크 단독 소유)
 *     ├─ MediaCodec AAC encoder → MediaMuxer → MP4 파일
 *     └─ ParcelFileDescriptor pipe → SpeechRecognizer (실시간 STT, API 33+)
 *
 * 안정성 보강:
 *   - startCapture 즉시 `running=true` (중복 호출 race 차단)
 *   - pipe write 는 별도 스레드 (SpeechRecognizer 가 느려도 capture 멈추지 않음)
 *   - 짧은 녹음에서 muxer 못 시작했으면 명시적으로 파일 제거
 *   - SpeechRecognizer 세션 재시작 시 콜백 race 차단 (token 비교)
 */
internal class RecordingService : Service() {

    private val audioFormat = AudioFormat.Builder()
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .setSampleRate(SAMPLE_RATE)
        .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
        .build()

    private var audioRecord: AudioRecord? = null
    private var captureThread: HandlerThread? = null
    private var pipeWriterThread: HandlerThread? = null
    private val pipeQueue = LinkedBlockingQueue<ByteArray>(64)
    private var encoder: MediaCodec? = null
    private var muxer: MediaMuxer? = null
    private var muxerTrackIndex: Int = -1
    private var muxerStarted = false
    private var presentationTimeUs: Long = 0L

    private val running = AtomicBoolean(false)
    private var outputPath: String? = null
    private var languageCode: String = "ko"
    private var recognizerSessionToken: Int = 0  // 세션 재시작 race 차단용

    private val mainHandler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    // pipeWrite (ParcelFileDescriptor) 와 pipeOut (FileOutputStream(pfd.fileDescriptor)) 는
    // 동일 fd 를 공유 → 둘 다 close 하면 double-close on same fd → 다른 thread 가 그 fd 를
    // 다른 리소스로 재할당하면 SIGSEGV. AutoCloseOutputStream 이 PFD ownership 을 흡수해서
    // single close. write 측은 pipeOut 하나로 통합 관리.
    private var pipeRead: ParcelFileDescriptor? = null
    private var pipeOut: OutputStream? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val path = intent.getStringExtra(EXTRA_OUTPUT_PATH)
                val lang = intent.getStringExtra(EXTRA_LANGUAGE) ?: "ko"
                if (path != null) {
                    // running 을 즉시 set — 중복 START 차단. compareAndSet 으로 atomic.
                    if (running.compareAndSet(false, true)) {
                        startCapture(path, lang)
                    } else {
                        android.util.Log.w(TAG, "ACTION_START ignored — already running")
                    }
                }
            }
            ACTION_STOP -> {
                if (running.get()) {
                    android.util.Log.i(TAG, "ACTION_STOP — initiating cleanup")
                    stopCaptureAndService()
                }
            }
        }
        return START_NOT_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startCapture(path: String, lang: String) {
        outputPath = path
        languageCode = lang
        _partialText.value = ""
        _confirmedText.value = ""
        muxerStarted = false
        muxerTrackIndex = -1
        presentationTimeUs = 0L
        pipeQueue.clear()
        recognizerSessionToken = 0
        cleanedUp = false

        startForegroundCompat()

        val minBuf = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        ).coerceAtLeast(4096)
        val bufferSize = (minBuf * 4).coerceAtLeast(BUFFER_BYTES_MIN)

        try {
            audioRecord = AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(bufferSize)
                .build()
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                throw IllegalStateException("AudioRecord not initialized")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "AudioRecord build failed", e)
            running.set(false)
            _state.value = RecordingState.Idle
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        try {
            setupEncoderAndMuxer(path)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Encoder/muxer setup failed", e)
            try { audioRecord?.release() } catch (_: Exception) {}
            audioRecord = null
            running.set(false)
            _state.value = RecordingState.Idle
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        if (Build.VERSION.SDK_INT >= 33) {
            try {
                val pipe = ParcelFileDescriptor.createPipe()
                pipeRead = pipe[0]
                // AutoCloseOutputStream 이 write 측 PFD 의 ownership 을 가져감 — close 한 번에 fd 깔끔 해제.
                pipeOut = ParcelFileDescriptor.AutoCloseOutputStream(pipe[1])
                pipeWriterThread = HandlerThread("rec-pipe").apply { start() }
                Handler(pipeWriterThread!!.looper).post { pipeWriterLoop() }
                mainHandler.post { startRecognizerSession() }
            } catch (e: Exception) {
                android.util.Log.w(TAG, "Pipe/SpeechRecognizer setup failed — fallback to file-only", e)
                closePipe()
            }
        } else {
            android.util.Log.i(TAG, "API < 33 — Live STT 비활성, 파일만 저장")
        }

        try {
            audioRecord?.startRecording()
            val recState = audioRecord?.recordingState
            if (recState != AudioRecord.RECORDSTATE_RECORDING) {
                throw IllegalStateException("AudioRecord not recording (state=$recState)")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "AudioRecord start failed", e)
            cleanup()
            return
        }
        _state.value = RecordingState.Recording(path)

        captureThread = HandlerThread("rec-capture").apply { start() }
        Handler(captureThread!!.looper).post { captureLoop(bufferSize) }
    }

    private fun captureLoop(bufferSize: Int) {
        val record = audioRecord
        if (record == null) {
            android.util.Log.e(TAG, "captureLoop: audioRecord is null")
            cleanup()
            return
        }
        val buf = ByteArray(bufferSize)
        var totalBytes = 0L
        var consecutiveBadReads = 0
        while (running.get()) {
            val read = try {
                record.read(buf, 0, buf.size)
            } catch (e: Exception) {
                android.util.Log.e(TAG, "AudioRecord.read err", e)
                -1
            }
            if (read <= 0) {
                consecutiveBadReads++
                if (consecutiveBadReads >= 50) {
                    android.util.Log.e(TAG, "Too many bad reads — aborting")
                    break
                }
                Thread.sleep(10)
                continue
            }
            consecutiveBadReads = 0
            totalBytes += read

            // pipe 는 별도 스레드 — capture 블락 안 됨. 큐 가득 차면 STT 만 drop.
            if (pipeOut != null) {
                val copy = buf.copyOf(read)
                if (!pipeQueue.offer(copy)) {
                    // queue full — STT 부담 → 가장 오래된 거 버리고 다시 시도
                    pipeQueue.poll()
                    pipeQueue.offer(copy)
                }
            }

            try { feedEncoder(buf, read) } catch (e: Exception) {
                android.util.Log.w(TAG, "encoder feed err", e)
            }
            try { drainEncoder(endOfStream = false) } catch (e: Exception) {
                android.util.Log.w(TAG, "drain err", e)
            }
        }
        android.util.Log.i(TAG, "captureLoop end — total bytes=$totalBytes muxerStarted=$muxerStarted")
        try { feedEncoderEos() } catch (e: Exception) { android.util.Log.w(TAG, "EOS err", e) }
        android.util.Log.d(TAG, "captureLoop: EOS fed")
        try { drainEncoder(endOfStream = true) } catch (e: Exception) { android.util.Log.w(TAG, "drain EOS err", e) }
        android.util.Log.d(TAG, "captureLoop: EOS drained")
        // cleanup 은 main thread 에서 — service lifecycle 메서드 안전 호출 보장.
        android.util.Log.d(TAG, "captureLoop: posting cleanup to main")
        mainHandler.post { cleanup() }
    }

    private fun pipeWriterLoop() {
        var totalWritten = 0L
        while (running.get() || pipeQueue.isNotEmpty()) {
            val chunk = try {
                pipeQueue.poll(50, java.util.concurrent.TimeUnit.MILLISECONDS)
            } catch (_: InterruptedException) { null } ?: continue
            try {
                pipeOut?.write(chunk)
                pipeOut?.flush()
                totalWritten += chunk.size
            } catch (e: Exception) {
                android.util.Log.w(TAG, "pipe write failed at $totalWritten bytes", e)
                closePipeQuiet()
                pipeQueue.clear()
                break
            }
        }
        android.util.Log.i(TAG, "pipeWriterLoop end — total written=$totalWritten")
        // EOF 를 SpeechRecognizer 에 알리기 위해 write 측 close
        try { pipeOut?.close() } catch (_: Exception) {}
        pipeOut = null
    }

    private fun setupEncoderAndMuxer(path: String) {
        val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, SAMPLE_RATE, 1).apply {
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            setInteger(MediaFormat.KEY_BIT_RATE, AAC_BITRATE)
            // 넉넉히 — AudioRecord 한 read 가 수십 KB 일 수 있음. 작게 잡으면 BufferOverflow.
            setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 65536)
        }
        encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC).apply {
            configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            start()
        }
        muxer = MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    }

    /**
     * AudioRecord 한 read 의 chunk 가 인코더 input buffer 보다 클 수 있어 (보통 8~32KB),
     * remaining 만큼만 잘라 여러 번 queueInputBuffer 로 분할 공급.
     */
    private fun feedEncoder(buf: ByteArray, length: Int) {
        val enc = encoder ?: return
        var offset = 0
        while (offset < length) {
            val inputBufferIndex = enc.dequeueInputBuffer(10_000)
            if (inputBufferIndex < 0) {
                // 인코더 busy — drainEncoder 가 곧 풀어줌. 다음 iteration 에서 재시도.
                return
            }
            val inputBuffer = enc.getInputBuffer(inputBufferIndex) ?: return
            inputBuffer.clear()
            val chunk = minOf(length - offset, inputBuffer.remaining())
            inputBuffer.put(buf, offset, chunk)
            val pts = presentationTimeUs
            presentationTimeUs += (chunk.toLong() * 1_000_000L) / (SAMPLE_RATE * 2L)
            enc.queueInputBuffer(inputBufferIndex, 0, chunk, pts, 0)
            offset += chunk
        }
    }

    private fun feedEncoderEos() {
        val enc = encoder ?: return
        val inputBufferIndex = enc.dequeueInputBuffer(10_000)
        if (inputBufferIndex >= 0) {
            enc.queueInputBuffer(
                inputBufferIndex, 0, 0, presentationTimeUs,
                MediaCodec.BUFFER_FLAG_END_OF_STREAM,
            )
        }
    }

    private fun drainEncoder(endOfStream: Boolean) {
        val enc = encoder ?: return
        val mux = muxer ?: return
        val info = MediaCodec.BufferInfo()
        val startNs = System.nanoTime()
        val maxDrainMs = if (endOfStream) 3_000L else 0L  // EOS 시 최대 3 초 대기
        while (true) {
            // 안전망: EOS drain 무한 루프 차단. encoder 가 EOS 신호 안 주면 그냥 종료.
            if (endOfStream && (System.nanoTime() - startNs) / 1_000_000L > maxDrainMs) {
                android.util.Log.w(TAG, "drainEncoder EOS timeout — abandoning")
                return
            }
            val outIdx = try {
                enc.dequeueOutputBuffer(info, if (endOfStream) 10_000 else 0)
            } catch (e: Exception) {
                android.util.Log.w(TAG, "dequeueOutputBuffer err", e)
                return
            }
            when {
                outIdx == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    if (!endOfStream) return
                    // EOS 일 땐 timeout 까지 계속 시도
                }
                outIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    if (muxerStarted) {
                        android.util.Log.w(TAG, "encoder format changed twice — ignoring")
                    } else {
                        muxerTrackIndex = mux.addTrack(enc.outputFormat)
                        mux.start()
                        muxerStarted = true
                    }
                }
                outIdx >= 0 -> {
                    val outBuf = enc.getOutputBuffer(outIdx)
                    if (outBuf == null) {
                        try { enc.releaseOutputBuffer(outIdx, false) } catch (_: Exception) {}
                        continue
                    }
                    if ((info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        info.size = 0
                    }
                    if (info.size > 0 && muxerStarted) {
                        try {
                            outBuf.position(info.offset)
                            outBuf.limit(info.offset + info.size)
                            mux.writeSampleData(muxerTrackIndex, outBuf, info)
                        } catch (e: Exception) {
                            android.util.Log.w(TAG, "muxer writeSampleData err", e)
                        }
                    }
                    try { enc.releaseOutputBuffer(outIdx, false) } catch (_: Exception) {}
                    if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) return
                }
            }
        }
    }

    private fun startRecognizerSession() {
        val read = pipeRead ?: return
        if (!running.get()) return
        recognizerSessionToken++
        val myToken = recognizerSessionToken
        try {
            val rec = SpeechRecognizer.createSpeechRecognizer(this)
            rec.setRecognitionListener(buildListener(myToken))
            // EXTRA_AUDIO_SOURCE 에 ParcelFileDescriptor 를 넘기면 SpeechRecognizer 가
            // ownership 을 가져가서 자기가 close 함. 우리가 또 close 하면 native
            // double-close → SIGSEGV. dup() 으로 별도 fd 를 만들어 SR 에게 주고,
            // pipeRead 는 우리가 계속 보유 (재시작 세션 때도 다시 dup 해서 넘김).
            val dupForSr = read.dup()
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, mapLocale(languageCode))
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                if (Build.VERSION.SDK_INT >= 33) {
                    putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, dupForSr)
                    putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_CHANNEL_COUNT, 1)
                    putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_ENCODING, AudioFormat.ENCODING_PCM_16BIT)
                    putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_SAMPLING_RATE, SAMPLE_RATE)
                }
            }
            rec.startListening(intent)
            recognizer = rec
            android.util.Log.i(TAG, "STT session started token=$myToken")
        } catch (e: Exception) {
            android.util.Log.w(TAG, "SpeechRecognizer start failed", e)
        }
    }

    private fun buildListener(myToken: Int) = object : RecognitionListener {
        override fun onReadyForSpeech(p: android.os.Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}

        override fun onPartialResults(partialResults: android.os.Bundle?) {
            if (myToken != recognizerSessionToken) return
            val text = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull().orEmpty()
            if (text.isEmpty()) return

            // SpeechRecognizer 의 EXTRA_AUDIO_SOURCE 모드에서 final result 가 거의 안 호출돼서,
            // partial 만 계속 옴. segment 가 바뀌면 새 partial 은 새 문장으로 시작하니
            // 이전 partial 을 confirmedText 로 flush 해야 누적이 안 사라짐.
            val prev = _partialText.value
            val prevTrim = prev.trim()
            val textTrim = text.trim()
            val isContinuation = prev.isEmpty() || textTrim.startsWith(prevTrim)
            if (!isContinuation && prevTrim.isNotEmpty()) {
                _confirmedText.value = (_confirmedText.value + " " + prevTrim).trim()
                android.util.Log.d(TAG, "STT segment flush: '$prevTrim' → confirmed")
            }
            _partialText.value = text
            android.util.Log.d(TAG, "STT partial: $text")
        }

        override fun onResults(results: android.os.Bundle?) {
            if (myToken != recognizerSessionToken) return
            val text = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull().orEmpty()
            android.util.Log.i(TAG, "STT result token=$myToken text='$text'")
            if (text.isNotEmpty()) {
                _confirmedText.value = (_confirmedText.value + " " + text).trim()
                _partialText.value = ""
            }
            recognizer?.destroy()
            recognizer = null
            if (running.get() && pipeRead != null) {
                mainHandler.postDelayed({ if (running.get()) startRecognizerSession() }, 50)
            }
        }

        override fun onError(error: Int) {
            if (myToken != recognizerSessionToken) return
            android.util.Log.w(TAG, "STT error token=$myToken code=$error (${errorName(error)})")
            recognizer?.destroy()
            recognizer = null
            if (running.get() && pipeRead != null && error != SpeechRecognizer.ERROR_CLIENT) {
                mainHandler.postDelayed({ if (running.get()) startRecognizerSession() }, 200)
            }
        }

        private fun errorName(code: Int): String = when (code) {
            SpeechRecognizer.ERROR_AUDIO -> "AUDIO"
            SpeechRecognizer.ERROR_CLIENT -> "CLIENT"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "INSUFFICIENT_PERMISSIONS"
            SpeechRecognizer.ERROR_NETWORK -> "NETWORK"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "NETWORK_TIMEOUT"
            SpeechRecognizer.ERROR_NO_MATCH -> "NO_MATCH"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RECOGNIZER_BUSY"
            SpeechRecognizer.ERROR_SERVER -> "SERVER"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "SPEECH_TIMEOUT"
            else -> "UNKNOWN($code)"
        }

        override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
    }

    private fun stopCaptureAndService() {
        android.util.Log.i(TAG, "stopCaptureAndService start")
        // partial 에 남은 텍스트도 confirmed 로 이관 (마지막 단어 손실 방지)
        if (_partialText.value.isNotEmpty()) {
            _confirmedText.value = (_confirmedText.value + " " + _partialText.value).trim()
            _partialText.value = ""
        }
        running.set(false)
        // SpeechRecognizer 콜백 race 차단 — 토큰 무효화 (실제 destroy 는 cleanup 에서).
        // Samsung 등 일부 디바이스에서 cancel() + destroy() 를 native side 가 처리 중일 때 호출하면
        // SIGSEGV. pipe write 측을 먼저 close 해서 SR 이 EOF 보고 자체적으로 idle 전이하도록 유도하면
        // 이후 destroy() 호출이 안전해짐. AutoCloseOutputStream 이라 fd close 도 single.
        recognizerSessionToken++
        try { pipeOut?.close() } catch (_: Exception) {}
        pipeOut = null
        android.util.Log.i(TAG, "stopCaptureAndService end — pipe write closed; captureLoop will drain + cleanup")
    }

    private var cleanedUp = false

    private fun cleanup() {
        if (cleanedUp) return
        cleanedUp = true
        android.util.Log.i(TAG, "cleanup start")

        // capture thread 종료를 먼저 보장 — 다른 native 객체와 동시 접근 막기 위함.
        // captureLoop 가 mainHandler.post { cleanup() } 직전 EOS drain 까진 끝낸 상태지만,
        // looper 자체는 아직 살아있을 수 있어 quitSafely + join 으로 완전 종료 확인.
        try { captureThread?.quitSafely() } catch (_: Exception) {}
        try { captureThread?.join(500) } catch (_: Exception) {}
        captureThread = null
        android.util.Log.d(TAG, "cleanup: captureThread done")

        // SpeechRecognizer: stopCaptureAndService 에서 pipe write 측 close 했으므로
        // SR 는 이미 EOF 처리 + idle 상태일 가능성 높음. cancel() 은 일부 디바이스(Samsung)에서
        // native crash 유발 → destroy() 만 호출. token 은 이미 stopCaptureAndService 에서 무효화됨.
        try { recognizer?.destroy() } catch (e: Exception) { android.util.Log.w(TAG, "recognizer.destroy err", e) }
        recognizer = null
        android.util.Log.d(TAG, "cleanup: recognizer done")

        try {
            audioRecord?.let { ar ->
                if (ar.recordingState == AudioRecord.RECORDSTATE_RECORDING) ar.stop()
            }
        } catch (e: Exception) { android.util.Log.w(TAG, "audioRecord.stop err", e) }
        try { audioRecord?.release() } catch (e: Exception) { android.util.Log.w(TAG, "audioRecord.release err", e) }
        audioRecord = null
        android.util.Log.d(TAG, "cleanup: audioRecord done")

        // encoder: flush → stop → release 순서. flush 가 pending buffer 비워서
        // stop 시점 native 측 자료구조 정리가 안전해짐 (일부 디바이스 SIGSEGV 회피).
        try { encoder?.flush() } catch (e: Exception) { android.util.Log.w(TAG, "encoder.flush err", e) }
        try { encoder?.stop() } catch (e: Exception) { android.util.Log.w(TAG, "encoder.stop err", e) }
        try { encoder?.release() } catch (e: Exception) { android.util.Log.w(TAG, "encoder.release err", e) }
        encoder = null
        android.util.Log.d(TAG, "cleanup: encoder done")

        // encoder release 와 muxer stop 사이 짧은 대기 — 일부 디바이스에서 native 측
        // codec 핸들 회수가 늦어 muxer stop 과 race 가 나는 케이스가 보고됨.
        try { Thread.sleep(20) } catch (_: InterruptedException) {}

        val muxOk = muxerStarted
        try { if (muxerStarted) muxer?.stop() } catch (e: Exception) { android.util.Log.w(TAG, "muxer.stop err", e) }
        try { muxer?.release() } catch (e: Exception) { android.util.Log.w(TAG, "muxer.release err", e) }
        muxer = null
        if (!muxOk) {
            outputPath?.let { p ->
                try { java.io.File(p).delete() } catch (_: Exception) {}
            }
            android.util.Log.w(TAG, "muxer never started — empty file deleted")
        }
        muxerStarted = false
        android.util.Log.d(TAG, "cleanup: muxer done muxOk=$muxOk")

        pipeQueue.clear()
        closePipe()
        try { pipeWriterThread?.quitSafely() } catch (_: Exception) {}
        try { pipeWriterThread?.join(300) } catch (_: Exception) {}
        pipeWriterThread = null
        android.util.Log.d(TAG, "cleanup: pipe done")

        _state.value = RecordingState.Idle
        try { stopForeground(STOP_FOREGROUND_REMOVE) } catch (e: Exception) { android.util.Log.w(TAG, "stopForeground err", e) }
        try { stopSelf() } catch (e: Exception) { android.util.Log.w(TAG, "stopSelf err", e) }
        android.util.Log.i(TAG, "cleanup done")
    }

    private fun closePipe() {
        try { pipeOut?.close() } catch (_: Exception) {}
        pipeOut = null
        // pipeRead: SR 에는 dup() 으로 전달했으므로 우리 PFD 는 단일 owner. close 안전.
        try { pipeRead?.close() } catch (_: Exception) {}
        pipeRead = null
    }

    private fun closePipeQuiet() {
        try { pipeOut?.close() } catch (_: Exception) {}
        pipeOut = null
    }

    private fun mapLocale(code: String): String = when (code) {
        "ko" -> "ko-KR"
        "en" -> "en-US"
        "ja" -> "ja-JP"
        else -> code
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
                    CHANNEL_ID, "녹음", NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = "메모 음성 녹음 진행 상태"
                    setSound(null, null)
                    enableVibration(false)
                }
                nm.createNotificationChannel(channel)
            }
        }
        val stopIntent = Intent(this, RecordingService::class.java).apply { action = ACTION_STOP }
        val stopPending = PendingIntent.getService(
            this, REQUEST_STOP, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Memozy 녹음 중")
            .setContentText("탭하여 정지")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_media_pause, "정지", stopPending)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        running.set(false)
    }

    companion object {
        const val ACTION_START = "me.pecos.memozy.recording.START"
        const val ACTION_STOP = "me.pecos.memozy.recording.STOP"
        const val EXTRA_OUTPUT_PATH = "output_path"
        const val EXTRA_LANGUAGE = "language"

        private const val CHANNEL_ID = "memozy.recording"
        private const val NOTIFICATION_ID = 8801
        private const val REQUEST_STOP = 1
        private const val TAG = "RecordingService"

        private const val SAMPLE_RATE = 16_000
        private const val AAC_BITRATE = 64_000
        private const val BUFFER_BYTES_MIN = 8192

        private val _state = MutableStateFlow<RecordingState>(RecordingState.Idle)
        val state: StateFlow<RecordingState> = _state

        private val _partialText = MutableStateFlow("")
        internal val partialText: StateFlow<String> = _partialText
        private val _confirmedText = MutableStateFlow("")
        internal val confirmedText: StateFlow<String> = _confirmedText

        fun start(context: Context, outputPath: String, language: String = "ko") {
            val intent = Intent(context, RecordingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_OUTPUT_PATH, outputPath)
                putExtra(EXTRA_LANGUAGE, language)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, RecordingService::class.java).apply { action = ACTION_STOP }
            context.startService(intent)
        }

        internal fun resetLiveText() {
            _partialText.value = ""
            _confirmedText.value = ""
        }
    }
}

internal sealed class RecordingState {
    data object Idle : RecordingState()
    data class Recording(val outputPath: String) : RecordingState()
}
