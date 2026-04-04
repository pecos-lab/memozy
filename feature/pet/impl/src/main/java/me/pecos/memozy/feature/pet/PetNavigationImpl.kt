package me.pecos.memozy.feature.pet

import androidx.navigation.NavGraphBuilder
import javax.inject.Inject

class PetNavigationImpl @Inject constructor() : PetNavigation {

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        onBack: () -> Unit
    ) {
        // Phase 2에서 구현
    }
}
