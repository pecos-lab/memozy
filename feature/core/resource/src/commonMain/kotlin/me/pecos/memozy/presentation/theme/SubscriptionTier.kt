package me.pecos.memozy.presentation.theme

enum class SubscriptionTier {
    FREE, PRO;

    val dailyAiLimit: Int
        get() = when (this) {
            FREE -> 3
            PRO -> 100
        }

    val isPro: Boolean get() = this == PRO
}
