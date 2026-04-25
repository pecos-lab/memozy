package me.pecos.memozy.presentation.theme

enum class SubscriptionTier {
    FREE, PRO;

    val dailyAiLimit: Int
        get() = when (this) {
            FREE -> 15
            PRO -> 100
        }

    val isPro: Boolean get() = this == PRO
}
