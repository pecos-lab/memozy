package me.pecos.memozy.feature.pet.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.ui.res.stringResource
import me.pecos.memozy.feature.core.resource.R
import me.pecos.memozy.feature.pet.model.PetUiState
import me.pecos.memozy.presentation.theme.LocalAppColors

/**
 * Hatch result screen — shown after egg breaks.
 * Character appears with fade-in, then profile card slides up.
 */
@Composable
fun HatchResultContent(
    pet: PetUiState,
    speciesName: String,
    rarityStars: String,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    var showCharacter by remember { mutableStateOf(false) }
    var showCard by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        showCharacter = true
        delay(600)
        showCard = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Character appear with fade
        AnimatedVisibility(
            visible = showCharacter,
            enter = fadeIn(animationSpec = tween(500))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "\u2728",
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\uD83D\uDE3A",
                    fontSize = 96.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Profile card slide up
        AnimatedVisibility(
            visible = showCard,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.cardBackground)
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.pet_new_friend),
                        color = colors.textTitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = speciesName,
                        color = colors.textTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = rarityStars,
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InfoChip(label = stringResource(R.string.pet_personality), value = pet.personality.lowercase()
                            .replaceFirstChar { it.uppercase() })
                        InfoChip(label = stringResource(R.string.pet_favorite), value = "#${pet.favoriteCategoryId}")
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = stringResource(R.string.pet_tap_to_name),
                        color = colors.chipText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.chipBackground)
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                            .fillMaxWidth()
                            .then(
                                Modifier.clickableNoRipple { onContinue() }
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    val colors = LocalAppColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = colors.textSecondary,
            fontSize = 11.sp
        )
        Text(
            text = value,
            color = colors.textTitle,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// Utility: clickable without ripple
@Composable
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier {
    return this.then(
        Modifier.clickable(
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    )
}
