package me.pecos.memozy.feature.pet

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import me.pecos.memozy.feature.pet.screen.PetHistoryScreen
import me.pecos.memozy.feature.pet.screen.PetScreen
import javax.inject.Inject

class PetNavigationImpl @Inject constructor() : PetNavigation {

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        onNavigateToHistory: () -> Unit,
        onBack: () -> Unit
    ) {
        navGraphBuilder.composable(PetRoute.PET) {
            val viewModel: PetViewModel = hiltViewModel()
            PetScreen(
                viewModel = viewModel,
                onNavigateToHistory = onNavigateToHistory
            )
        }
        navGraphBuilder.composable(PetRoute.HISTORY) {
            val viewModel: PetViewModel = hiltViewModel()
            PetHistoryScreen(
                viewModel = viewModel,
                onBack = onBack
            )
        }
    }
}
