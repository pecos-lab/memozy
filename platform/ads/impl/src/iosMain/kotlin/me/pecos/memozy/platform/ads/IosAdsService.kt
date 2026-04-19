package me.pecos.memozy.platform.ads

/**
 * iOS no-op 구현 (이슈 #280 옵션 A).
 * 리워드 광고 SDK 연동 전까지 모든 메서드는 아무 동작도 하지 않고,
 * 상위 UI는 [isPlatformSupported] 로 iOS 미지원 안내를 분기한다.
 */
class IosAdsService : AdsService {

    override val isAdReady: Boolean = false
    override val isAdLoading: Boolean = false
    override val isPlatformSupported: Boolean = false

    override fun initialize() { /* no-op */ }
    override fun loadAd() { /* no-op */ }
    override fun showAd(onRewarded: () -> Unit) { /* no-op — 호출되지 않아야 함 */ }

    override fun bindActivity(activity: Any?) { /* no-op */ }
    override fun unbindActivity() { /* no-op */ }
}
