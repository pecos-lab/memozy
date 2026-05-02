import Foundation
import Speech
import AVFoundation
import Shared

/// SFSpeechRecognizer + AVAudioEngine 으로 실시간 받아쓰기.
/// SFSpeech 세션은 ~60초 한도. 한 세션이 끝나면 (final result 또는 timeout) 즉시 새 세션 시작.
final class IosLiveTranscriptionBridge: LiveTranscriptionBridge {
    private var recognizer: SFSpeechRecognizer?
    private let audioEngine = AVAudioEngine()
    private var request: SFSpeechAudioBufferRecognitionRequest?
    private var task: SFSpeechRecognitionTask?
    private var audioFile: AVAudioFile?  // Gemini polish 용 WAV 파일

    private var stillListening = false
    private var languageCode: String = "ko"
    private var sessionConfirmedPrefix: String = ""
    private var outputPath: String?

    func start(languageCode: String, outputPath: String?) {
        NSLog("[LiveSTT] start lang=\(languageCode) outputPath=\(outputPath ?? "nil")")
        self.languageCode = languageCode
        self.outputPath = outputPath
        self.stillListening = true
        self.sessionConfirmedPrefix = ""

        // 1. 권한 확인 (마이크 + 음성 인식)
        SFSpeechRecognizer.requestAuthorization { [weak self] status in
            guard let self = self else { return }
            DispatchQueue.main.async {
                NSLog("[LiveSTT] auth status raw=\(status.rawValue)")
                switch status {
                case .authorized:
                    NSLog("[LiveSTT] authorized — beginning session")
                    self.beginSession()
                case .denied:
                    NSLog("[LiveSTT] DENIED")
                    LiveTranscriptionRegistrar.shared.onError(message: "Speech permission denied")
                case .restricted:
                    LiveTranscriptionRegistrar.shared.onError(message: "Speech recognition restricted")
                case .notDetermined:
                    LiveTranscriptionRegistrar.shared.onError(message: "Speech permission not determined")
                @unknown default:
                    LiveTranscriptionRegistrar.shared.onError(message: "Unknown permission status")
                }
            }
        }
    }

    func stop() {
        stillListening = false
        endSession(final: true)
    }

    private func beginSession() {
        NSLog("[LiveSTT] beginSession()")
        let locale = mapLocale(languageCode)
        guard let rec = SFSpeechRecognizer(locale: locale) else {
            NSLog("[LiveSTT] recognizer init nil for \(locale.identifier)")
            LiveTranscriptionRegistrar.shared.onError(message: "Recognizer not available for \(locale.identifier)")
            return
        }
        guard rec.isAvailable else {
            NSLog("[LiveSTT] recognizer.isAvailable = false")
            LiveTranscriptionRegistrar.shared.onError(message: "Recognizer temporarily unavailable")
            return
        }
        self.recognizer = rec

        // AVAudioSession 설정 — 단순화 (default mode, 옵션 없음). AVAudioRecorder 와 호환되게.
        let session = AVAudioSession.sharedInstance()
        do {
            try session.setCategory(.playAndRecord, mode: .default, options: [.allowBluetooth, .defaultToSpeaker])
            try session.setActive(true, options: .notifyOthersOnDeactivation)
            NSLog("[LiveSTT] session set OK")
        } catch {
            NSLog("[LiveSTT] session error: \(error.localizedDescription)")
            LiveTranscriptionRegistrar.shared.onError(message: "Audio session: \(error.localizedDescription)")
            return
        }

        // 인식 요청
        let req = SFSpeechAudioBufferRecognitionRequest()
        req.shouldReportPartialResults = true
        if #available(iOS 13.0, *) {
            req.requiresOnDeviceRecognition = false // 정확도 우선 — 네트워크 사용
        }
        self.request = req

        // 오디오 엔진 — 인식 + (옵션) WAV 파일 동시 캡처
        let inputNode = audioEngine.inputNode
        let recordingFormat = inputNode.outputFormat(forBus: 0)
        inputNode.removeTap(onBus: 0) // 안전: 기존 tap 정리

        // Gemini polish 용 WAV 파일 셋업 (outputPath 있을 때만)
        if let path = outputPath {
            do {
                let url = URL(fileURLWithPath: path)
                self.audioFile = try AVAudioFile(forWriting: url, settings: recordingFormat.settings)
                NSLog("[LiveSTT] audioFile opened at \(path)")
            } catch {
                NSLog("[LiveSTT] audioFile open failed: \(error.localizedDescription) — Gemini polish 비활성")
                self.audioFile = nil
            }
        }

        inputNode.installTap(onBus: 0, bufferSize: 1024, format: recordingFormat) { [weak self] buffer, _ in
            self?.request?.append(buffer)
            // 동시에 파일 쓰기 — 인식과 똑같은 buffer 를 그대로 저장
            if let file = self?.audioFile {
                try? file.write(from: buffer)
            }
        }

        audioEngine.prepare()
        do {
            try audioEngine.start()
            NSLog("[LiveSTT] audio engine started")
        } catch {
            NSLog("[LiveSTT] engine start error: \(error.localizedDescription)")
            LiveTranscriptionRegistrar.shared.onError(message: "Audio engine: \(error.localizedDescription)")
            return
        }

        // 인식 task 시작
        task = rec.recognitionTask(with: req) { [weak self] result, error in
            guard let self = self else { return }
            if let result = result {
                let text = result.bestTranscription.formattedString
                NSLog("[LiveSTT] result text='\(text)' isFinal=\(result.isFinal)")
                if result.isFinal {
                    // 한 세션 종료 — 누적 confirmed 로 이관
                    if !text.isEmpty {
                        LiveTranscriptionRegistrar.shared.onConfirmed(appendText: text)
                    }
                    self.endSession(final: false)
                    if self.stillListening {
                        // 즉시 새 세션 (60초 한도 + 무음 종료 후 끊김 없게)
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.05) {
                            if self.stillListening { self.beginSession() }
                        }
                    }
                } else {
                    LiveTranscriptionRegistrar.shared.onPartial(text: text)
                }
            }
            if let error = error {
                self.endSession(final: false)
                if self.stillListening {
                    // 일시적 에러는 재시작
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                        if self.stillListening { self.beginSession() }
                    }
                } else {
                    LiveTranscriptionRegistrar.shared.onError(message: error.localizedDescription)
                }
            }
        }

        LiveTranscriptionRegistrar.shared.onListening()
    }

    private func endSession(final: Bool) {
        if audioEngine.isRunning {
            audioEngine.stop()
            audioEngine.inputNode.removeTap(onBus: 0)
        }
        request?.endAudio()
        task?.cancel()
        task = nil
        request = nil
        recognizer = nil

        if final {
            // 파일 close (deinit 으로 flush)
            audioFile = nil
            try? AVAudioSession.sharedInstance().setActive(false, options: .notifyOthersOnDeactivation)
            LiveTranscriptionRegistrar.shared.onIdle()
            NSLog("[LiveSTT] session ended (final), audioFile closed")
        }
    }

    private func mapLocale(_ code: String) -> Locale {
        switch code {
        case "ko": return Locale(identifier: "ko-KR")
        case "en": return Locale(identifier: "en-US")
        case "ja": return Locale(identifier: "ja-JP")
        default:   return Locale(identifier: code)
        }
    }
}
