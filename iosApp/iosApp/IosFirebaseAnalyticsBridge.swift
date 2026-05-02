import Foundation
import FirebaseAnalytics
import Shared

final class IosFirebaseAnalyticsBridge: AnalyticsBridge {
    func logEvent(name: String, params: [String : Any]) {
        // FirebaseAnalytics 는 String/Number 만 안전하게 받음 — 그 외는 String 변환
        var safeParams: [String: Any] = [:]
        for (key, value) in params {
            // Bool 은 NSNumber 서브클래스라 NSNumber 분기가 먼저 잡으면 0/1 로 기록됨 → Bool 먼저
            switch value {
            case let b as Bool: safeParams[key] = b
            case let s as String: safeParams[key] = s
            case let n as NSNumber: safeParams[key] = n
            default: safeParams[key] = String(describing: value)
            }
        }
        Analytics.logEvent(name, parameters: safeParams.isEmpty ? nil : safeParams)
    }

    func setUserId(userId: String?) {
        Analytics.setUserID(userId)
    }

    func setUserProperty(name: String, value: String?) {
        Analytics.setUserProperty(value, forName: name)
    }
}
