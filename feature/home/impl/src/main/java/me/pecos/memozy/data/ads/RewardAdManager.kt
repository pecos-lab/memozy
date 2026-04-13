package me.pecos.memozy.data.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.pecos.memozy.presentation.theme.RewardAdProvider

sealed class RewardAdState {
    data object NotLoaded : RewardAdState()
    data object Loading : RewardAdState()
    data object Ready : RewardAdState()
    data object Showing : RewardAdState()
    data object Rewarded : RewardAdState()
    data class Error(val message: String) : RewardAdState()
}

class RewardAdManager(
    private val context: Context,
    private val activity: Activity
) : RewardAdProvider {

    companion object {
        // 테스트 광고 단위 ID — 출시 시 실제 ID로 교체
        private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
        const val MAX_DAILY_AD_VIEWS = 3
    }

    private var rewardedAd: RewardedAd? = null

    private val _state = MutableStateFlow<RewardAdState>(RewardAdState.NotLoaded)
    val state: StateFlow<RewardAdState> = _state

    override val isAdReady: Boolean get() = _state.value is RewardAdState.Ready
    override val isAdLoading: Boolean get() = _state.value is RewardAdState.Loading

    override fun loadAd() {
        if (_state.value is RewardAdState.Loading || _state.value is RewardAdState.Ready) return

        _state.value = RewardAdState.Loading

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, AD_UNIT_ID, adRequest, object : RewardedAdLoadCallback() {
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

        ad.show(activity) {
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
