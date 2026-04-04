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
import androidx.compose.ui.res.stringResource
import me.pecos.memozy.feature.core.resource.R
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

            ProfileRow(stringResource(R.string.pet_species), viewModel.getSpeciesName(pet.speciesId))
            ProfileRow(stringResource(R.string.pet_personality), pet.personality.lowercase().replaceFirstChar { it.uppercase() })
            ProfileRow(stringResource(R.string.pet_favorite), "Category #${pet.favoriteCategoryId}")
            ProfileRow(stringResource(R.string.pet_dislike), pet.dislike.replace("_", " "))
            ProfileRow(stringResource(R.string.pet_level), "Lv.${pet.level}")
            ProfileRow(stringResource(R.string.pet_mood), "${pet.mood}/100")
            ProfileRow(stringResource(R.string.pet_days_together), "D+${viewModel.getDaysTogether(pet)}")

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
