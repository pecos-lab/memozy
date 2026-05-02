package me.pecos.memozy.platform.analytics

interface AnalyticsService {
    fun logEvent(name: String, params: Map<String, Any> = emptyMap())
    fun setUserId(userId: String?)
    fun setUserProperty(name: String, value: String?)
}

object AnalyticsEvents {
    const val MEMO_CREATED = "memo_created"
    const val MEMO_UPDATED = "memo_updated"
    const val MEMO_DELETED = "memo_deleted"

    const val AI_SUMMARY_REQUESTED = "ai_summary_requested"
    const val YOUTUBE_SUMMARY_REQUESTED = "youtube_summary_requested"
    const val WEB_SUMMARY_REQUESTED = "web_summary_requested"

    const val LOGIN_SUCCEEDED = "login_succeeded"
    const val LOGIN_FAILED = "login_failed"
    const val LOGOUT = "logout"

    const val SUBSCRIPTION_VIEWED = "subscription_viewed"
    const val SUBSCRIPTION_PURCHASED = "subscription_purchased"

    const val AI_LIMIT_REACHED = "ai_limit_reached"
    const val AD_REWARDED_WATCHED = "ad_rewarded_watched"
}

object AnalyticsParams {
    const val SOURCE = "source"
    const val PROVIDER = "provider"
    const val SUMMARY_STYLE = "summary_style"
    const val PRODUCT_ID = "product_id"
    const val FEATURE = "feature"
    const val ERROR_MESSAGE = "error_message"
}
