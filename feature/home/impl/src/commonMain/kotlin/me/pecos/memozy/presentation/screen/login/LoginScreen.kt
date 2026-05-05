package me.pecos.memozy.presentation.screen.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import kotlinx.coroutines.launch
import me.pecos.memozy.feature.core.resource.generated.resources.Res
import me.pecos.memozy.feature.core.resource.generated.resources.ic_google
import me.pecos.memozy.feature.core.resource.generated.resources.login_skip
import me.pecos.memozy.feature.core.resource.generated.resources.login_subtitle
import me.pecos.memozy.feature.core.resource.generated.resources.sign_in_error
import me.pecos.memozy.feature.core.resource.generated.resources.sign_in_apple
import me.pecos.memozy.feature.core.resource.generated.resources.sign_in_google
import me.pecos.memozy.feature.home.impl.GOOGLE_WEB_CLIENT_ID
import me.pecos.memozy.feature.home.impl.IsGoogleSignInAvailable
import me.pecos.memozy.platform.credential.AppleSignInResult
import me.pecos.memozy.platform.credential.CredentialService
import me.pecos.memozy.platform.credential.GoogleSignInResult
import me.pecos.memozy.platform.intent.ToastPresenter
import me.pecos.memozy.presentation.theme.LocalActivity
import me.pecos.memozy.presentation.theme.LocalAppColors
import me.pecos.memozy.presentation.theme.LocalFontSettings
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun LoginScreen(
    onSignIn: (idToken: String) -> Unit,
    onAppleSignIn: (idToken: String, rawNonce: String) -> Unit = { _, _ -> },
    onSkip: () -> Unit,
) {
    val colors = LocalAppColors.current
    val fontSettings = LocalFontSettings.current
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    val credentialService: CredentialService = koinInject()
    val toastPresenter: ToastPresenter = koinInject()

    Scaffold(
        containerColor = colors.screenBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Memozy",
                fontSize = fontSettings.scaled(32),
                fontWeight = FontWeight.Bold,
                color = colors.topbarTitle
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.login_subtitle),
                fontSize = fontSettings.scaled(14),
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (IsGoogleSignInAvailable) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val result = credentialService.signInWithGoogle(
                                activity = activity,
                                serverClientId = GOOGLE_WEB_CLIENT_ID,
                            )
                            when (result) {
                                is GoogleSignInResult.Success -> onSignIn(result.idToken)
                                is GoogleSignInResult.Cancelled -> Unit
                                is GoogleSignInResult.Error -> {
                                    println("LoginScreen: Sign-in failed: ${result.message}")
                                    toastPresenter.show(getString(Res.string.sign_in_error))
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, colors.cardBorder),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textTitle),
                ) {
                    Icon(painter = painterResource(Res.drawable.ic_google), contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.sign_in_google), fontSize = fontSettings.scaled(14))
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Apple 로 계속하기 — Apple HIG: 검정 배경 + 사과 로고. iOS 에서만 실제 동작.
            // Android 는 OAuth Web flow (Custom Tabs) 미구현이라 버튼 자체를 숨김 (#330 Option C).
            if (credentialService.isAppleSignInAvailable) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val result = credentialService.signInWithApple(activity = activity)
                            when (result) {
                                is AppleSignInResult.Success -> onAppleSignIn(result.idToken, result.rawNonce)
                                is AppleSignInResult.Cancelled -> Unit
                                is AppleSignInResult.Error -> {
                                    println("LoginScreen: Apple sign-in failed: ${result.message}")
                                    toastPresenter.show(getString(Res.string.sign_in_error))
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, colors.cardBorder),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textTitle),
                ) {
                    Text("\uF8FF", fontSize = fontSettings.scaled(16))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.sign_in_apple), fontSize = fontSettings.scaled(14))
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, colors.cardBorder),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textSecondary),
            ) {
                Text(stringResource(Res.string.login_skip), fontSize = fontSettings.scaled(14))
            }
        }
    }
}
