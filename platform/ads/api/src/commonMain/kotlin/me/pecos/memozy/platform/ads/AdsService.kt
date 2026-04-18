package me.pecos.memozy.platform.ads

interface AdsService {
    val isAdReady: Boolean
    val isAdLoading: Boolean

    fun initialize()
    fun loadAd()
    fun showAd(onRewarded: () -> Unit)

    fun bindActivity(activity: Any?)
    fun unbindActivity()
}
