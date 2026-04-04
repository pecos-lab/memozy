package me.pecos.memozy.feature.pet.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import me.pecos.memozy.feature.core.resource.R
import me.pecos.memozy.feature.pet.PetViewModel
import me.pecos.memozy.feature.pet.model.PetUiState
import me.pecos.memozy.presentation.theme.LocalAppColors

/**
 * Departure confirmation screen.
 * Shows pet stats and asks user to confirm sending the pet away.
 */
@Composable
fun DepartContent(
    pet: PetUiState,
    viewModel: PetViewModel,
    onConfirmDepart: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val daysTogether = viewModel.getDaysTogether(pet)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.pet_departing, pet.name),
            color = colors.textTitle,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Pet waving goodbye
        Text(
            text = "\uD83D\uDC4B",
            fontSize = 72.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Stats card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.cardBackground)
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                StatRow(stringResource(R.string.pet_days_together_stat), "D+$daysTogether")
                StatRow(stringResource(R.string.pet_level_reached), "Lv.${pet.level}")
                StatRow(stringResource(R.string.pet_species), viewModel.getSpeciesName(pet.speciesId))
                StatRow("Rarity", viewModel.getRarityStars(pet.rarity))

                Spacer(modifier = Modifier.height(16.dp))

                // Last words
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.chipBackground)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "\uD83D\uDCAC \"${stringResource(R.string.pet_last_words)}\"",
                        color = colors.chipText,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.pet_stay_together))
            }
            Button(
                onClick = onConfirmDepart,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE57373)
                )
            ) {
                Text(stringResource(R.string.pet_say_goodbye), color = Color.White)
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = colors.textSecondary,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = colors.textTitle,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
