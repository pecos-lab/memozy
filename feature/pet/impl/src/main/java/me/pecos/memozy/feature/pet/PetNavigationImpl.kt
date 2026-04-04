package me.pecos.memozy.feature.pet

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import me.pecos.memozy.feature.pet.screen.PetScreen
import javax.inject.Inject

class PetNavigationImpl @Inject constructor() : PetNavigation {

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        onBack: () -> Unit
    ) {
        navGraphBuilder.composable(PetRoute.PET) {
            val viewModel: PetViewModel = hiltViewModel()
            PetScreen(viewModel = viewModel)
        }
    }
}
