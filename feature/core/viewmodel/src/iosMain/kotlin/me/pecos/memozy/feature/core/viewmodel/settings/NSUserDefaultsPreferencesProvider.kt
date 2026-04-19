package me.pecos.memozy.feature.core.viewmodel.settings

import platform.Foundation.NSUserDefaults

class NSUserDefaultsPreferencesProvider(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) : PreferencesProvider {
    override fun getString(key: String, defaultValue: String): String =
        defaults.stringForKey(key) ?: defaultValue

    override fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        // objectForKey 가 null 이면 defaultValue, 아니면 boolForKey.
        return if (defaults.objectForKey(key) == null) defaultValue
        else defaults.boolForKey(key)
    }

    override fun putBoolean(key: String, value: Boolean) {
        defaults.setBool(value, forKey = key)
    }
}
