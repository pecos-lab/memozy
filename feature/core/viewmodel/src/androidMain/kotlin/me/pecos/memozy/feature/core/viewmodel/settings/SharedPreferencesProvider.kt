package me.pecos.memozy.feature.core.viewmodel.settings

import android.content.Context

class SharedPreferencesProvider(
    context: Context,
    preferencesName: String = "settings",
) : PreferencesProvider {

    private val prefs = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)

    override fun getString(key: String, defaultValue: String): String =
        prefs.getString(key, defaultValue) ?: defaultValue

    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        prefs.getBoolean(key, defaultValue)

    override fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }
}
