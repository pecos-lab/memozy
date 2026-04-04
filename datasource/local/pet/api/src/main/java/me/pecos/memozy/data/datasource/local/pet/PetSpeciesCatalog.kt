package me.pecos.memozy.data.datasource.local.pet

object PetSpeciesCatalog {

    val ALL: List<PetSpecies> = listOf(
        // ★☆☆☆☆ (rarity 1) — 35%
        PetSpecies(
            id = "sea_jelly",
            nameResId = 0, // TODO: R.string.species_sea_jelly
            descriptionResId = 0,
            rarity = 1,
            baseRiveAsset = "sea_jelly.riv",
            availablePersonalities = listOf(Personality.SHY, Personality.GENTLE)
        ),
        PetSpecies(
            id = "dust_bunny",
            nameResId = 0,
            descriptionResId = 0,
            rarity = 1,
            baseRiveAsset = "dust_bunny.riv",
            availablePersonalities = listOf(Personality.PLAYFUL, Personality.SHY)
        ),
        PetSpecies(
            id = "leaf_snail",
            nameResId = 0,
            descriptionResId = 0,
            rarity = 1,
            baseRiveAsset = "leaf_snail.riv",
            availablePersonalities = listOf(Personality.GENTLE, Personality.SHY)
        ),

        // ★★☆☆☆ (rarity 2) — 30%
        PetSpecies(
            id = "star_bunny",
            nameResId = 0,
            descriptionResId = 0,
            rarity = 2,
            baseRiveAsset = "star_bunny.riv",
            availablePersonalities = listOf(Personality.PLAYFUL, Personality.ACTIVE, Personality.SHY)
        ),
        PetSpecies(
            id = "cotton_chick",
            nameResId = 0,
            descriptionResId = 0,
            rarity = 2,
            baseRiveAsset = "cotton_chick.riv",
            availablePersonalities = listOf(Personality.ACTIVE, Personality.GENTLE)
        ),
        PetSpecies(
            id = "rain_frog",
            nameResId = 0,
            descriptionResId = 0,
            rarity = 2,
            baseRiveAsset = "rain_frog.riv",
            availablePersonalities = listOf(Personality.PLAYFUL, Personality.PROUD)
        ),

        // ★★★☆☆ (rarity 3) — 20%
        PetSpecies(
            id = "cloud_fox",
            nameResId = 0,
            descriptionResId = 0,
            rarity = 3,
            baseRiveAsset = "cloud_fox.riv",
            availablePersonalities = listOf(Personality.ACTIVE, Personality.PLAYFUL, Personality.SHY)
        ),
        PetSpecies(
            id = "flame_hamster",
            nameResId = 0,
            descriptionResId = 0,
            rarity = 3,
            baseRiveAsset = "flame_hamster.riv",
            availablePersonalities = listOf(Personality.ACTIVE, Personality.PROUD)
        ),
        PetSpecies(
            id = "wind_owl",
            nameResId = 0,
            descriptionResId = 0,
            rarity = 3,
            baseRiveAsset = "wind_owl.riv",
            availablePersonalities = listOf(Personality.PROUD, Personality.GENTLE, Personality.SHY)
        ),

        // ★★★★☆ (rarity 4) — 12%
        PetSpecies(
            id = "forest_bear",
            nameResId = 0,
            descriptionResId = 0,
            rarity = 4,
            baseRiveAsset = "forest_bear.riv",
            availablePersonalities = listOf(Personality.GENTLE, Personality.PROUD, Personality.ACTIVE)
        ),
        PetSpecies(
            id = "aurora_deer",
            nameResId = 0,
            descriptionResId = 0,
            rarity = 4,
            baseRiveAsset = "aurora_deer.riv",
            availablePersonalities = listOf(Personality.GENTLE, Personality.SHY, Personality.PROUD)
        ),

        // ★★★★★ (rarity 5) — 3%
        PetSpecies(
            id = "moon_cat",
            nameResId = 0,
            descriptionResId = 0,
            rarity = 5,
            baseRiveAsset = "moon_cat.riv",
            availablePersonalities = Personality.entries
        ),
    )

    fun getByRarity(rarity: Int): List<PetSpecies> = ALL.filter { it.rarity == rarity }

    fun getById(id: String): PetSpecies? = ALL.find { it.id == id }
}
