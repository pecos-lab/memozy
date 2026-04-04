package me.pecos.memozy.data.datasource.local.pet

object PetSpeciesCatalog {

    val ALL: List<PetSpecies> = listOf(
        PetSpecies(
            id = "dog",
            nameResId = 0,
            descriptionResId = 0,
            rarity = 1,
            baseRiveAsset = "dog.riv",
            availablePersonalities = Personality.entries
        ),
        PetSpecies(
            id = "cat",
            nameResId = 0,
            descriptionResId = 0,
            rarity = 1,
            baseRiveAsset = "cat.riv",
            availablePersonalities = Personality.entries
        ),
    )

    fun getByRarity(rarity: Int): List<PetSpecies> = ALL.filter { it.rarity == rarity }

    fun getById(id: String): PetSpecies? = ALL.find { it.id == id }

    fun getEmojiForSpecies(id: String): String = when (id) {
        "dog" -> "\uD83D\uDC36"
        "cat" -> "\uD83D\uDC31"
        else -> "\uD83D\uDC3E"
    }

    fun getDisplayName(id: String): String = when (id) {
        "dog" -> "Dog"
        "cat" -> "Cat"
        else -> id
    }
}
