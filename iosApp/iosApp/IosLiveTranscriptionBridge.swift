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

    private var stillListening = false
    private var languageCode: String = "ko"
    private var sessionConfirmedPrefix: String = ""

    func start(languageCode: String) {
        self.languageCode = languageCode
        self.stillListening = true
        self.sessionConfirmedPrefix = ""

        // 1. 권한 확인 (마이크 + 음성 인식)
        SFSpeechRecognizer.requestAuthorization { [weak self] status in
            guard let self = self else { return }
            DispatchQueue.main.async {
                switch status {
                case .authorized:
                    self.beginSession()
                case .denied, .restricted, .notDetermined:
                    LiveTranscriptionRegistrar.shared.onError(message: "Speech permission not granted")
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
        let locale = mapLocale(languageCode)
        guard let rec = SFSpeechRecognizer(locale: locale) else {
            LiveTranscriptionRegistrar.shared.onError(message: "Recognizer not available for \(locale.identifier)")
            return
        }
        guard rec.isAvailable else {
            LiveTranscriptionRegistrar.shared.onError(message: "Recognizer temporarily unavailable")
            return
        }
        self.recognizer = rec

        // AVAudioSession 설정
        let session = AVAudioSession.sharedInstance()
        do {
            try session.setCategory(.playAndRecord, mode: .measurement, options: .duckOthers)
            try session.setActive(true, options: .notifyOthersOnDeactivation)
        } catch {
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

        // 오디오 엔진 — 인식 + 입력 노드 tap 설치
        let inputNode = audioEngine.inputNode
        let recordingFormat = inputNode.outputFormat(forBus: 0)
        inputNode.removeTap(onBus: 0) // 안전: 기존 tap 정리
        inputNode.installTap(onBus: 0, bufferSize: 1024, format: recordingFormat) { [weak self] buffer, _ in
            self?.request?.append(buffer)
        }

        audioEngine.prepare()
        do {
            try audioEngine.start()
        } catch {
            LiveTranscriptionRegistrar.shared.onError(message: "Audio engine: \(error.localizedDescription)")
            return
        }

        // 인식 task 시작
        task = rec.recognitionTask(with: req) { [weak self] result, error in
            guard let self = self else { return }
            if let result = result {
                let text = result.bestTranscription.formattedString
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
            try? AVAudioSession.sharedInstance().setActive(false, options: .notifyOthersOnDeactivation)
            LiveTranscriptionRegistrar.shared.onIdle()
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
