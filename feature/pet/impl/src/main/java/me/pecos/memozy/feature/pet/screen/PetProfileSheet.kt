package me.pecos.memozy.feature.pet.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.pecos.memozy.feature.pet.PetViewModel
import me.pecos.memozy.feature.pet.model.PetUiState
import me.pecos.memozy.presentation.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetProfileSheet(
    pet: PetUiState,
    viewModel: PetViewModel,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.cardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = pet.name,
                color = colors.textTitle,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = viewModel.getRarityStars(pet.rarity),
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            ProfileRow("Species", viewModel.getSpeciesName(pet.speciesId))
            ProfileRow("Personality", pet.personality.lowercase().replaceFirstChar { it.uppercase() })
            ProfileRow("Favorite", "Category #${pet.favoriteCategoryId}")
            ProfileRow("Dislike", pet.dislike.replace("_", " "))
            ProfileRow("Level", "Lv.${pet.level}")
            ProfileRow("Mood", "${pet.mood}/100")
            ProfileRow("Days Together", "D+${viewModel.getDaysTogether(pet)}")

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = colors.textSecondary,
            fontSize = 14.sp,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            color = colors.textTitle,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
