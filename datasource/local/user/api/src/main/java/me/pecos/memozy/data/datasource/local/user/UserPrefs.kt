package me.pecos.memozy.data.datasource.local.user

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object UserPrefs {
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val USER_LEVEL = intPreferencesKey("user_level") // 1: 초보, 2: 중급, 3: 고급
    val TOTAL_AI_CHATS = intPreferencesKey("total_ai_chats")
    val PROMPTS_IMPROVED = intPreferencesKey("prompts_improved")
}
