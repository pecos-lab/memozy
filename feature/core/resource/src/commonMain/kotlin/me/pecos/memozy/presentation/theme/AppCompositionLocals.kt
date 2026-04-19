package me.pecos.memozy.presentation.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

val LocalSubscriptionTier = compositionLocalOf { SubscriptionTier.FREE }
val LocalIsLoggedIn = compositionLocalOf { false }
val LocalLanguageCode = compositionLocalOf { "ko" }
val LocalActivity = staticCompositionLocalOf<Any?> { null }
