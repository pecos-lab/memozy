package me.pecos.memozy.presentation.screen.login

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer as SpacerImport
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.launch
import me.pecos.memozy.feature.core.resource.R
import me.pecos.memozy.feature.home.impl.BuildConstants
import me.pecos.memozy.platform.credential.CredentialService
import me.pecos.memozy.platform.credential.GoogleSignInResult
import me.pecos.memozy.presentation.theme.LocalActivity
import me.pecos.memozy.presentation.theme.LocalAppColors
import me.pecos.memozy.presentation.theme.LocalFontSettings
import org.koin.compose.koinInject

@Composable
fun LoginScreen(
    onSignIn: (idToken: String) -> Unit,
    onSkip: () -> Unit,
) {
    val colors = LocalAppColors.current
    val fontSettings = LocalFontSettings.current
    val context = LocalContext.current
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    val credentialService: CredentialService = koinInject()

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
                text = stringResource(R.string.login_subtitle),
                fontSize = fontSettings.scaled(14),
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedButton(
                onClick = {
                    scope.launch {
                        val result = credentialService.signInWithGoogle(
                            activity = activity ?: context,
                            serverClientId = BuildConstants.GOOGLE_WEB_CLIENT_ID,
                        )
                        when (result) {
                            is GoogleSignInResult.Success -> onSignIn(result.idToken)
                            is GoogleSignInResult.Cancelled -> Unit
                            is GoogleSignInResult.Error -> {
                                android.util.Log.e("LoginScreen", "Sign-in failed: ${result.message}")
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.sign_in_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, colors.cardBorder),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textTitle),
            ) {
                Icon(painter = painterResource(R.drawable.ic_google), contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.sign_in_google), fontSize = fontSettings.scaled(14))
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, colors.cardBorder),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textSecondary),
            ) {
                Text(stringResource(R.string.login_skip), fontSize = fontSettings.scaled(14))
            }
        }
    }
}
