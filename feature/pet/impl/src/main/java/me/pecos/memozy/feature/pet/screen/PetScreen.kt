package me.pecos.memozy.feature.pet.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.pecos.memozy.feature.pet.PetViewModel
import me.pecos.memozy.feature.pet.model.PetScreenState

@Composable
fun PetScreen(
    viewModel: PetViewModel,
    onNavigateToHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val screenState by viewModel.screenState.collectAsState()
    val petUiState by viewModel.petUiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        when (screenState) {
            PetScreenState.LOADING -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            PetScreenState.NO_PET -> {
                SpeciesSelectContent(
                    onSelectSpecies = { speciesId -> viewModel.hatchPetWithSpecies(speciesId) }
                )
            }
            PetScreenState.HATCHING -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            PetScreenState.HATCH_RESULT -> {
                HatchResultContent(
                    pet = petUiState,
                    speciesName = viewModel.getSpeciesName(petUiState.speciesId),
                    rarityStars = viewModel.getRarityStars(petUiState.rarity),
                    onContinue = { viewModel.proceedToNaming() }
                )
            }
            PetScreenState.NAMING -> {
                NamePetContent(
                    speciesName = viewModel.getSpeciesName(petUiState.speciesId),
                    rarity = petUiState.rarity,
                    rarityStars = viewModel.getRarityStars(petUiState.rarity),
                    onConfirm = { name -> viewModel.namePet(name) }
                )
            }
            PetScreenState.ACTIVE -> {
                PetMainContent(
                    pet = petUiState,
                    viewModel = viewModel,
                    onNavigateToHistory = onNavigateToHistory
                )
            }
            PetScreenState.DEPARTING -> {
                DepartContent(
                    pet = petUiState,
                    viewModel = viewModel,
                    onConfirmDepart = { viewModel.rerollPet() },
                    onCancel = { viewModel.cancelDeparting() }
                )
            }
        }
    }
}
