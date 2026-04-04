package me.pecos.memozy.feature.pet.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import me.pecos.memozy.feature.pet.impl.R as PetR
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.pecos.memozy.feature.core.resource.R
import me.pecos.memozy.feature.pet.PetViewModel
import me.pecos.memozy.feature.pet.model.Condition
import me.pecos.memozy.feature.pet.model.PetDialogue
import me.pecos.memozy.feature.pet.model.PetUiState
import me.pecos.memozy.feature.pet.model.TimeOfDay
import me.pecos.memozy.feature.pet.model.TouchReaction
import me.pecos.memozy.feature.pet.rive.RivePetView
import me.pecos.memozy.presentation.theme.LocalAppColors

@Composable
fun PetMainContent(
    pet: PetUiState,
    viewModel: PetViewModel,
    onNavigateToHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val moodState = viewModel.getMoodState(pet.mood)
    val condition = viewModel.getCondition(pet.mood)
    val timeOfDay = viewModel.getTimeOfDay()
    var showProfile by remember { mutableStateOf(false) }

    // Touch reaction state
    val scope = rememberCoroutineScope()
    var touchDialogue by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Top: Name + Level + Rarity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Lv.${pet.level} ${pet.name}",
                    color = colors.textTitle,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = viewModel.getRarityStars(pet.rarity),
                    color = colors.textSecondary,
                    fontSize = 16.sp
                )
            }
            Text(
                text = "D+${viewModel.getDaysTogether(pet)}",
                color = colors.textSecondary,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // EXP Bar (mood bar removed — condition is invisible)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "EXP",
                color = colors.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(8.dp))
            LinearProgressIndicator(
                progress = { viewModel.getExpProgress(pet) },
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                trackColor = colors.cardBorder,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = viewModel.getExpText(pet),
                color = colors.textSecondary,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Character Area — touch triggers condition-based reaction
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.cardBackground)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    viewModel.interactWithPet()

                    val reaction = viewModel.getTouchReaction(pet.personality, pet.mood)
                    touchDialogue = reaction.dialogue.random()

                    scope.launch {
                        delay(2000)
                        touchDialogue = null
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Pet character
            RivePetView(
                riveAssetName = "${pet.speciesId}.riv",
                mood = pet.mood,
                timeOfDay = when (timeOfDay) {
                    TimeOfDay.MORNING -> 0
                    TimeOfDay.DAY -> 1
                    TimeOfDay.EVENING -> 2
                    TimeOfDay.NIGHT -> 3
                },
                isTouching = false,
                modifier = Modifier.fillMaxSize(),
                fallbackContent = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val drawableRes = when (pet.speciesId) {
                            "dog" -> PetR.drawable.pet_dog
                            "cat" -> PetR.drawable.pet_cat
                            else -> PetR.drawable.pet_dog
                        }
                        Image(
                            painter = painterResource(id = drawableRes),
                            contentDescription = viewModel.getSpeciesName(pet.speciesId),
                            modifier = Modifier.size(200.dp)
                        )
                    }
                }
            )

            // Touch dialogue popup
            touchDialogue?.let { dialogue ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.chipBackground)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "\uD83D\uDCAC \"$dialogue\"",
                        color = colors.chipText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { showProfile = true },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.pet_profile))
            }
            OutlinedButton(
                onClick = onNavigateToHistory,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.pet_memories))
            }
            OutlinedButton(
                onClick = { viewModel.startDeparting() },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.pet_reroll))
            }
        }
    }

    if (showProfile) {
        PetProfileSheet(
            pet = pet,
            viewModel = viewModel,
            onDismiss = { showProfile = false }
        )
    }
}

private fun getConditionEmoji(condition: Condition, time: TimeOfDay): String {
    if (time == TimeOfDay.NIGHT) return "\uD83D\uDE34"
    return when (condition) {
        Condition.HIGH -> "\uD83D\uDE3B"    // beaming cat
        Condition.MEDIUM -> "\uD83D\uDE3A"  // smiling cat
        Condition.LOW -> "\uD83D\uDE3E"     // pouting cat
    }
}
