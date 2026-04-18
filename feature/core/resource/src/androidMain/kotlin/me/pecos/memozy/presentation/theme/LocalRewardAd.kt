package me.pecos.memozy.presentation.theme

import androidx.compose.runtime.compositionLocalOf

/**
 * 리워드 광고 상태를 앱 전역에서 참조하기 위한 인터페이스.
 * feature/home/impl의 RewardAdManager가 이를 구현합니다.
 */
interface RewardAdProvider {
    val isAdReady: Boolean
    val isAdLoading: Boolean
    fun loadAd()
    fun showAd(onRewarded: () -> Unit)
}

val LocalRewardAdProvider = compositionLocalOf<RewardAdProvider?> { null }
