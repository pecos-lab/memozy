package me.pecos.memozy.platform.ads

interface AdsService {
    val isAdReady: Boolean
    val isAdLoading: Boolean

    /** 현재 플랫폼에서 광고 기능을 실제로 지원하는지. iOS no-op 구현은 false. */
    val isPlatformSupported: Boolean get() = true

    fun initialize()
    fun loadAd()
    fun showAd(onRewarded: () -> Unit)

    fun bindActivity(activity: Any?)
    fun unbindActivity()
}
