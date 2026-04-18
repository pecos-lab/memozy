package me.pecos.memozy.platform.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class RewardAdState {
    data object NotLoaded : RewardAdState()
    data object Loading : RewardAdState()
    data object Ready : RewardAdState()
    data object Showing : RewardAdState()
    data object Rewarded : RewardAdState()
    data class Error(val message: String) : RewardAdState()
}

fun provideAdsService(context: Context): AdsService = AndroidAdsService(context)

internal class AndroidAdsService(
    context: Context,
) : AdsService {

    private companion object {
        // 테스트 광고 단위 ID — 출시 시 실제 ID로 교체
        const val AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    }

    private val appContext = context.applicationContext

    private var rewardedAd: RewardedAd? = null
    private var activity: Activity? = null

    private val _state = MutableStateFlow<RewardAdState>(RewardAdState.NotLoaded)
    val state: StateFlow<RewardAdState> = _state

    override val isAdReady: Boolean get() = _state.value is RewardAdState.Ready
    override val isAdLoading: Boolean get() = _state.value is RewardAdState.Loading

    override fun initialize() {
        MobileAds.initialize(appContext)
    }

    override fun bindActivity(activity: Any?) {
        this.activity = activity as? Activity
    }

    override fun unbindActivity() {
        this.activity = null
    }

    override fun loadAd() {
        if (_state.value is RewardAdState.Loading || _state.value is RewardAdState.Ready) return

        _state.value = RewardAdState.Loading

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(appContext, AD_UNIT_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                _state.value = RewardAdState.Ready
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                rewardedAd = null
                _state.value = RewardAdState.Error(error.message)
            }
        })
    }

    override fun showAd(onRewarded: () -> Unit) {
        val ad = rewardedAd ?: run {
            _state.value = RewardAdState.Error("광고가 준비되지 않았어요.")
            return
        }
        val hostActivity = activity ?: run {
            _state.value = RewardAdState.Error("광고를 표시할 Activity가 없어요.")
            return
        }

        _state.value = RewardAdState.Showing

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                _state.value = RewardAdState.Error(error.message)
                loadAd()
            }
        }

        ad.show(hostActivity) {
            _state.value = RewardAdState.Rewarded
            onRewarded()
        }
    }

    fun resetState() {
        if (_state.value is RewardAdState.Rewarded || _state.value is RewardAdState.Error) {
            _state.value = if (rewardedAd != null) RewardAdState.Ready else RewardAdState.NotLoaded
        }
    }
}
