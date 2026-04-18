package me.pecos.memozy.feature.core.viewmodel.settings

interface PreferencesProvider {
    fun getString(key: String, defaultValue: String): String
    fun putString(key: String, value: String)
}
