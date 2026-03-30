package me.pecos.memozy.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Memo : BottomNavItem(
        route = "main",
        label = "Memo",
        icon = Icons.Default.Edit
    )
    object Settings : BottomNavItem(
        route = "settings",
        label = "Settings",
        icon = Icons.Default.Settings
    )
}

val bottomNavItems = listOf(BottomNavItem.Memo, BottomNavItem.Settings)
