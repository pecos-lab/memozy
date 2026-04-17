package me.pecos.memozy.presentation.theme

enum class SubscriptionTier {
    FREE, PRO;

    val dailyAiLimit: Int
        get() = when (this) {
            FREE -> 100
            PRO -> 15
        }

    val isPro: Boolean get() = this == PRO
}
