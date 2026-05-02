package me.pecos.memozy.platform.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

fun provideAnalyticsService(context: Context): AnalyticsService =
    AndroidAnalyticsService(context)

internal class AndroidAnalyticsService(context: Context) : AnalyticsService {
    private val firebase: FirebaseAnalytics = Firebase.analytics

    override fun logEvent(name: String, params: Map<String, Any>) {
        val bundle = Bundle().apply {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Float -> putFloat(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> putString(key, value.toString())
                }
            }
        }
        firebase.logEvent(name, bundle)
    }

    override fun setUserId(userId: String?) {
        firebase.setUserId(userId)
    }

    override fun setUserProperty(name: String, value: String?) {
        firebase.setUserProperty(name, value)
    }
}
