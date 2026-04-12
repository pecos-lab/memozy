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
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.wanted.android.wanted.design.actions.button.WantedButton
import com.wanted.android.wanted.design.actions.button.config.WantedButtonDefaults
import com.wanted.android.wanted.design.util.ButtonType
import com.wanted.android.wanted.design.util.ButtonVariant
import kotlinx.coroutines.launch
import me.pecos.memozy.feature.core.resource.R
import me.pecos.memozy.feature.home.impl.BuildConfig
import me.pecos.memozy.presentation.theme.LocalActivity
import me.pecos.memozy.presentation.theme.LocalAppColors

@Composable
fun LoginScreen(
    onSignIn: (idToken: String) -> Unit,
    onSkip: () -> Unit,
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()

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
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = colors.topbarTitle
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.login_subtitle),
                fontSize = 14.sp,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            WantedButton(
                text = stringResource(R.string.sign_in_google),
                modifier = Modifier.fillMaxWidth(),
                buttonDefault = WantedButtonDefaults.getDefault(
                    type = ButtonType.ASSISTIVE,
                    variant = ButtonVariant.OUTLINED
                ),
                onClick = {
                    scope.launch {
                        try {
                            val credentialManager = CredentialManager.create(context)
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                                .setFilterByAuthorizedAccounts(false)
                                .build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()
                            val result = credentialManager.getCredential(
                                context = activity ?: context,
                                request = request,
                            )
                            val googleIdToken = GoogleIdTokenCredential
                                .createFrom(result.credential.data)
                                .idToken
                            onSignIn(googleIdToken)
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.sign_in_error),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            WantedButton(
                text = stringResource(R.string.login_skip),
                modifier = Modifier.fillMaxWidth(),
                buttonDefault = WantedButtonDefaults.getDefault(
                    type = ButtonType.ASSISTIVE,
                    variant = ButtonVariant.OUTLINED
                ).copy(contentColor = colors.textSecondary),
                onClick = onSkip
            )
        }
    }
}
