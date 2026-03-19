package me.pecos.nota

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val iconRes: Int  // TODO: 아이콘 추가 후 R.drawable.xxx 넣기
) {
    object Memo : BottomNavItem(
        route = "main",
        label = "Memo",
        iconRes = R.drawable.memo
    )
    object Settings : BottomNavItem(
        route = "settings",
        label = "Settings",
        iconRes = R.drawable.setting
    )
}

val bottomNavItems = listOf(BottomNavItem.Memo, BottomNavItem.Settings)
