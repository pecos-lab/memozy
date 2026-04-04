package me.pecos.memozy.feature.pet.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.pecos.memozy.feature.pet.rive.RiveEggView
import me.pecos.memozy.presentation.theme.LocalAppColors

@Composable
fun GachaContent(
    onHatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val scope = rememberCoroutineScope()
    var tapCount by remember { mutableIntStateOf(0) }
    val shakeOffset = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(1f) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (tapCount == 0) "Tap the egg!" else "Keep tapping! (${tapCount}/3)",
            color = colors.textTitle,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(160.dp)
                .offset { IntOffset(shakeOffset.value.toInt(), 0) }
                .scale(scaleAnim.value)
                .clip(RoundedCornerShape(32.dp))
                .background(colors.cardBackground)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    scope.launch {
                        val newCount = tapCount + 1
                        tapCount = newCount

                        // Shake animation
                        launch {
                            shakeOffset.animateTo(
                                targetValue = 20f,
                                animationSpec = tween(50)
                            )
                            shakeOffset.animateTo(
                                targetValue = -20f,
                                animationSpec = tween(50)
                            )
                            shakeOffset.animateTo(
                                targetValue = 10f,
                                animationSpec = tween(50)
                            )
                            shakeOffset.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(stiffness = Spring.StiffnessHigh)
                            )
                        }

                        if (newCount >= 3) {
                            // Hatch: scale up then callback
                            delay(200)
                            scaleAnim.animateTo(
                                targetValue = 1.3f,
                                animationSpec = tween(200)
                            )
                            scaleAnim.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(150)
                            )
                            onHatch()
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Rive egg animation with emoji fallback
            RiveEggView(
                tapCount = tapCount,
                modifier = Modifier.fillMaxSize(),
                fallbackContent = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (tapCount < 3) "\uD83E\uDD5A" else "\uD83D\uDCA5",
                            fontSize = 72.sp
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Who's waiting inside?",
            color = colors.textSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}
