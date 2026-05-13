package me.pecos.memozy.presentation.theme

enum class SubscriptionTier {
    FREE, PRO;

    val dailyAiLimit: Int
        get() = when (this) {
            // TEMP: 출시 전 테스트 기간 임시 한도. 출시 시 FREE=3 / PRO=30 으로 복원.
            FREE -> 100
            PRO -> 1000
        }

    val isPro: Boolean get() = this == PRO
}
