package me.pecos.memozy.data.datasource.local.pet

data class PetSpecies(
    val id: String,
    val nameResId: Int,
    val descriptionResId: Int,
    val rarity: Int,
    val baseRiveAsset: String,
    val availablePersonalities: List<Personality>
)
