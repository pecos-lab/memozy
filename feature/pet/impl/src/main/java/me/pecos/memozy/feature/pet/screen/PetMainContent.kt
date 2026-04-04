package me.pecos.memozy.feature.pet.screen

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.foundation.border
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import me.pecos.memozy.feature.pet.PetViewModel
import me.pecos.memozy.feature.pet.model.MoodState
import me.pecos.memozy.feature.pet.model.PetUiState
import me.pecos.memozy.feature.pet.model.TimeOfDay
import me.pecos.memozy.presentation.theme.LocalAppColors

@Composable
fun PetMainContent(
    pet: PetUiState,
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val moodState = viewModel.getMoodState(pet.mood)
    val timeOfDay = viewModel.getTimeOfDay()
    var showProfile by remember { mutableStateOf(false) }

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

        // EXP Bar
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

        Spacer(modifier = Modifier.height(8.dp))

        // Mood Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getMoodEmoji(moodState),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            LinearProgressIndicator(
                progress = { pet.mood / 100f },
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                trackColor = colors.cardBorder,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${pet.mood}/100",
                color = colors.textSecondary,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Character Area (Placeholder for Phase 3 Rive)
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
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = getPetEmoji(moodState, timeOfDay),
                    fontSize = 80.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = getTimeGreeting(timeOfDay),
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Speech Bubble
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.chipBackground)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = getSpeechText(moodState, pet.name),
                color = colors.chipText,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
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
                Text("Profile")
            }
            OutlinedButton(
                onClick = { /* Phase 4: History */ },
                modifier = Modifier.weight(1f)
            ) {
                Text("Memories")
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

private fun getMoodEmoji(mood: MoodState): String = when (mood) {
    MoodState.HAPPY -> "\u2764\uFE0F"
    MoodState.NORMAL -> "\uD83D\uDE0A"
    MoodState.LONELY -> "\uD83D\uDE14"
    MoodState.SAD -> "\uD83D\uDE22"
}

private fun getPetEmoji(mood: MoodState, time: TimeOfDay): String {
    if (time == TimeOfDay.NIGHT) return "\uD83D\uDE34"
    return when (mood) {
        MoodState.HAPPY -> "\uD83D\uDE3B"
        MoodState.NORMAL -> "\uD83D\uDE3A"
        MoodState.LONELY -> "\uD83D\uDE3E"
        MoodState.SAD -> "\uD83D\uDE40"
    }
}

private fun getTimeGreeting(time: TimeOfDay): String = when (time) {
    TimeOfDay.MORNING -> "Good morning~ *stretches*"
    TimeOfDay.DAY -> "Let's write some memos!"
    TimeOfDay.EVENING -> "Getting sleepy~ *yawn*"
    TimeOfDay.NIGHT -> "Zzz..."
}

private fun getSpeechText(mood: MoodState, name: String): String = when (mood) {
    MoodState.HAPPY -> "\uD83D\uDCAC \"Write more memos today~!\""
    MoodState.NORMAL -> "\uD83D\uDCAC \"Hey there, $name is here!\""
    MoodState.LONELY -> "\uD83D\uDCAC \"It's been a while... I missed you.\""
    MoodState.SAD -> "\uD83D\uDCAC \"...\""
}
