package me.pecos.memozy.platform.analytics

/**
 * iOS Analytics — Firebase iOS SDK 호출은 Swift 브릿지로 위임.
 * iosApp 의 IosFirebaseAnalyticsBridge.swift 가 [bridge] 에 set.
 *
 * 브릿지 미설정 시 no-op (Firebase 미통합 환경 / 시뮬레이터 등에서 안전).
 */
interface AnalyticsBridge {
    fun logEvent(name: String, params: Map<String, Any>)
    fun setUserId(userId: String?)
    fun setUserProperty(name: String, value: String?)
}

object AnalyticsRegistrar {
    var bridge: AnalyticsBridge? = null
}

class IosAnalyticsService : AnalyticsService {
    override fun logEvent(name: String, params: Map<String, Any>) {
        AnalyticsRegistrar.bridge?.logEvent(name, params)
    }

    override fun setUserId(userId: String?) {
        AnalyticsRegistrar.bridge?.setUserId(userId)
    }

    override fun setUserProperty(name: String, value: String?) {
        AnalyticsRegistrar.bridge?.setUserProperty(name, value)
    }
}
