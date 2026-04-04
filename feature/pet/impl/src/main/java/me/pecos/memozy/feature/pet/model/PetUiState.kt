package me.pecos.memozy.feature.pet.model

data class PetUiState(
    val id: Int = 0,
    val speciesId: String = "",
    val name: String = "",
    val personality: String = "",
    val favoriteCategoryId: Int = 0,
    val dislike: String = "",
    val catchphrase: String = "",
    val rarity: Int = 1,
    val mood: Int = 70,
    val exp: Int = 0,
    val level: Int = 1,
    val lastFedAt: Long = 0L,
    val lastInteractionAt: Long = 0L,
    val birthday: Long = 0L,
    val isActive: Boolean = true
)

enum class PetScreenState {
    LOADING,
    NO_PET,
    HATCHING,
    HATCH_RESULT,
    NAMING,
    ACTIVE,
    DEPARTING
}

enum class TimeOfDay {
    MORNING,   // 06~09
    DAY,       // 09~18
    EVENING,   // 18~22
    NIGHT      // 22~06
}

enum class MoodState(val label: String) {
    HAPPY("happy"),
    NORMAL("normal"),
    LONELY("lonely"),
    SAD("sad");

    companion object {
        fun fromMood(mood: Int): MoodState = when {
            mood >= 80 -> HAPPY
            mood >= 50 -> NORMAL
            mood >= 20 -> LONELY
            else -> SAD
        }
    }
}
