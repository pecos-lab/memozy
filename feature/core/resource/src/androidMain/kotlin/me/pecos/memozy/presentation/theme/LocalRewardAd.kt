package me.pecos.memozy.presentation.theme

import androidx.compose.runtime.compositionLocalOf
import me.pecos.memozy.platform.ads.AdsService

/**
 * 리워드 광고 제공자를 앱 전역에서 참조하기 위한 CompositionLocal.
 * 실제 구현은 platform/ads/impl의 AndroidAdsService가 담당한다.
 */
val LocalRewardAdProvider = compositionLocalOf<AdsService?> { null }
