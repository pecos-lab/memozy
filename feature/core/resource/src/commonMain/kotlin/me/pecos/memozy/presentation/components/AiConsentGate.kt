package me.pecos.memozy.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.pecos.memozy.feature.core.resource.generated.resources.Res
import me.pecos.memozy.feature.core.resource.generated.resources.ai_consent_agree
import me.pecos.memozy.feature.core.resource.generated.resources.ai_consent_decline
import me.pecos.memozy.feature.core.resource.generated.resources.ai_consent_description
import me.pecos.memozy.feature.core.resource.generated.resources.ai_consent_title
import me.pecos.memozy.feature.core.viewmodel.settings.AiConsentKeys
import me.pecos.memozy.feature.core.viewmodel.settings.PreferencesProvider
import me.pecos.memozy.presentation.theme.LocalAppColors
import org.jetbrains.compose.resources.stringResource

/**
 * App Store Guideline 5.1.1(i)/5.1.2(i) 대응 — 제3자 AI 서비스(Gemini, Cloudflare Workers,
 * Supadata)에 사용자 데이터를 보내기 전 사전 동의를 받는 게이트.
 *
 * 최초 진입 시 동의 결정([AiConsentKeys.DECIDED])이 없으면 모달을 띄우고,
 * 사용자가 선택하면 [AiConsentKeys.GIVEN] / [AiConsentKeys.DECIDED] 를 저장한다.
 * 미동의 상태에서 AI 호출 시 [me.pecos.memozy.data.datasource.remote.ai.AIException.ConsentRequiredException]
 * 이 던져진다 (AIApiServiceImpl 의 requireConsent 가드).
 *
 * Android: MainActivity NavHost 옆, iOS: AppNavHost 시작부에 한 번 호출.
 */
@Composable
fun AiConsentGate(prefs: PreferencesProvider) {
    var decided by remember {
        mutableStateOf(prefs.getBoolean(AiConsentKeys.DECIDED, false))
    }
    if (decided) return

    val colors = LocalAppColors.current
    AppPopup(
        // 사전 동의 모달은 백 탭/배경 탭으로 닫혀선 안 됨 → onDismiss no-op.
        onDismissRequest = { },
        title = stringResource(Res.string.ai_consent_title),
        navigation = PopupNavigation.EMPHASIZED,
        size = PopupSize.LARGE,
        actionArea = PopupActionArea.STRONG,
        primaryButtonText = stringResource(Res.string.ai_consent_agree),
        onPrimaryClick = {
            prefs.putBoolean(AiConsentKeys.GIVEN, true)
            prefs.putBoolean(AiConsentKeys.DECIDED, true)
            decided = true
        },
        secondaryButtonText = stringResource(Res.string.ai_consent_decline),
        onSecondaryClick = {
            prefs.putBoolean(AiConsentKeys.GIVEN, false)
            prefs.putBoolean(AiConsentKeys.DECIDED, true)
            decided = true
        },
    ) {
        Text(
            text = stringResource(Res.string.ai_consent_description),
            color = colors.textBody,
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}
