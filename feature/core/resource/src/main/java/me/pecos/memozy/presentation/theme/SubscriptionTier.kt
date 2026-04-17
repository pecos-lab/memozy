package me.pecos.memozy.presentation.theme

import androidx.compose.runtime.compositionLocalOf

enum class SubscriptionTier {
    FREE, PRO;

    val dailyAiLimit: Int
        get() = when (this) {
            FREE -> 100
            PRO -> 15
        }

    val isPro: Boolean get() = this == PRO
}

val LocalSubscriptionTier = compositionLocalOf { SubscriptionTier.FREE }
val LocalIsLoggedIn = compositionLocalOf { false }
