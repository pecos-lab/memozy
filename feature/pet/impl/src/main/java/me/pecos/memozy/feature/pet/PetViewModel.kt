package me.pecos.memozy.feature.pet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.pecos.memozy.data.datasource.local.pet.PetSpeciesCatalog
import me.pecos.memozy.data.datasource.local.pet.entity.Pet
import me.pecos.memozy.data.repository.pet.PetRepository
import me.pecos.memozy.feature.pet.model.Condition
import me.pecos.memozy.feature.pet.model.MoodState
import me.pecos.memozy.feature.pet.model.PetScreenState
import me.pecos.memozy.feature.pet.model.PetUiState
import me.pecos.memozy.feature.pet.model.TimeOfDay
import me.pecos.memozy.feature.pet.model.TouchReaction
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class PetViewModel @Inject constructor(
    private val petRepository: PetRepository
) : ViewModel() {

    private val _screenState = MutableStateFlow(PetScreenState.LOADING)
    val screenState: StateFlow<PetScreenState> = _screenState

    val activePet = petRepository.getActivePet()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val petUiState: StateFlow<PetUiState> = combine(
        activePet, _screenState
    ) { pet, _ ->
        pet?.toUiState() ?: PetUiState()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PetUiState())

    init {
        viewModelScope.launch {
            activePet.collect { pet ->
                _screenState.value = when {
                    pet == null -> PetScreenState.NO_PET
                    pet.name.isBlank() -> PetScreenState.NAMING
                    else -> PetScreenState.ACTIVE
                }
            }
        }
        viewModelScope.launch {
            petRepository.updateMood()
        }
    }

    fun hatchPet() {
        viewModelScope.launch {
            _screenState.value = PetScreenState.HATCHING
            petRepository.hatchPet()
            _screenState.value = PetScreenState.HATCH_RESULT
        }
    }

    fun proceedToNaming() {
        _screenState.value = PetScreenState.NAMING
    }

    fun namePet(name: String) {
        viewModelScope.launch {
            petRepository.namePet(name)
        }
    }

    fun startDeparting() {
        _screenState.value = PetScreenState.DEPARTING
    }

    fun cancelDeparting() {
        _screenState.value = PetScreenState.ACTIVE
    }

    fun rerollPet() {
        viewModelScope.launch {
            petRepository.rerollPet()
            _screenState.value = PetScreenState.HATCH_RESULT
        }
    }

    fun interactWithPet() {
        viewModelScope.launch {
            petRepository.interactWithPet()
        }
    }

    val petHistory = petRepository.getPetHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getSpeciesName(speciesId: String): String {
        return PetSpeciesCatalog.getDisplayName(speciesId)
    }

    fun getSpeciesEmoji(speciesId: String): String {
        return PetSpeciesCatalog.getEmojiForSpecies(speciesId)
    }

    fun getTimeOfDay(): TimeOfDay {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour in 6..8 -> TimeOfDay.MORNING
            hour in 9..17 -> TimeOfDay.DAY
            hour in 18..21 -> TimeOfDay.EVENING
            else -> TimeOfDay.NIGHT
        }
    }

    fun getMoodState(mood: Int): MoodState = MoodState.fromMood(mood)

    fun getCondition(mood: Int): Condition = Condition.fromMood(mood)

    fun getTouchReaction(personality: String, mood: Int): TouchReaction.ReactionData {
        return TouchReaction.getReaction(personality, Condition.fromMood(mood))
    }

    fun onAppForeground() {
        viewModelScope.launch {
            val pet = petRepository.getActivePet().first() ?: return@launch
            val newMood = (pet.mood + 3).coerceAtMost(100)
            if (newMood != pet.mood) {
                petRepository.interactWithPet() // small boost on app open
            }
        }
    }

    fun getExpProgress(pet: PetUiState): Float {
        val needed = pet.level * 100
        return if (needed > 0) pet.exp.toFloat() / needed else 0f
    }

    fun getExpText(pet: PetUiState): String {
        return "${pet.exp} / ${pet.level * 100}"
    }

    fun getDaysTogether(pet: PetUiState): Int {
        if (pet.birthday == 0L) return 0
        return ((System.currentTimeMillis() - pet.birthday) / 86_400_000L).toInt()
    }

    fun getRarityStars(rarity: Int): String = "\u2605".repeat(rarity) + "\u2606".repeat(5 - rarity)
}

private fun Pet.toUiState() = PetUiState(
    id = id,
    speciesId = speciesId,
    name = name,
    personality = personality,
    favoriteCategoryId = favoriteCategoryId,
    dislike = dislike,
    catchphrase = catchphrase,
    rarity = rarity,
    mood = mood,
    exp = exp,
    level = level,
    lastFedAt = lastFedAt,
    lastInteractionAt = lastInteractionAt,
    birthday = birthday,
    isActive = isActive
)
