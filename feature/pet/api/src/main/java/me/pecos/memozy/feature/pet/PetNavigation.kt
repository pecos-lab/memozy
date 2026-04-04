package me.pecos.memozy.feature.pet

import androidx.navigation.NavGraphBuilder

object PetRoute {
    const val PET = "pet"
    const val GACHA = "pet/gacha"
    const val HISTORY = "pet/history"
}

interface PetNavigation {
    fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        onBack: () -> Unit
    )
}
