import Foundation
import FirebaseAnalytics
import Shared

final class IosFirebaseAnalyticsBridge: AnalyticsBridge {
    func logEvent(name: String, params: [String : Any]) {
        // FirebaseAnalytics 는 String/Number 만 안전하게 받음 — 그 외는 String 변환
        var safeParams: [String: Any] = [:]
        for (key, value) in params {
            switch value {
            case let s as String: safeParams[key] = s
            case let n as NSNumber: safeParams[key] = n
            case let b as Bool: safeParams[key] = b
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
