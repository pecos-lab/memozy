package me.pecos.memozy.presentation.theme

import androidx.compose.runtime.staticCompositionLocalOf

enum class SubscriptionTier {
    FREE, PRO;

    val dailyAiLimit: Int
        get() = when (this) {
            FREE -> 2
            PRO -> 15
        }

    val isPro: Boolean get() = this == PRO
}

val LocalSubscriptionTier = staticCompositionLocalOf { SubscriptionTier.FREE }
