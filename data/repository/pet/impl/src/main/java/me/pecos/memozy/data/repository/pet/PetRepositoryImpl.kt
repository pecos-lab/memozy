package me.pecos.memozy.data.repository.pet

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import me.pecos.memozy.data.datasource.local.pet.PetDao
import me.pecos.memozy.data.datasource.local.pet.PetSpeciesCatalog
import me.pecos.memozy.data.datasource.local.pet.entity.Pet
import me.pecos.memozy.data.datasource.local.pet.entity.PetHistory
import javax.inject.Inject
import kotlin.random.Random

class PetRepositoryImpl @Inject constructor(
    private val petDao: PetDao
) : PetRepository {

    override fun getActivePet(): Flow<Pet?> = petDao.getActivePet()

    override suspend fun hatchPet(): Pet {
        val rarity = rollRarity()
        val species = PetSpeciesCatalog.getByRarity(rarity).random()
        val personality = species.availablePersonalities.random()

        val pet = Pet(
            speciesId = species.id,
            name = "",
            personality = personality.name,
            favoriteCategoryId = Random.nextInt(1, 12),
            dislike = DISLIKES.random(),
            catchphrase = CATCHPHRASES.random(),
            rarity = species.rarity,
            birthday = System.currentTimeMillis()
        )
        petDao.insertPet(pet)
        return petDao.getActivePet().first()!!
    }

    override suspend fun rerollPet(): Pet {
        val currentPet = petDao.getActivePet().first()
        if (currentPet != null) {
            val daysTogether = ((System.currentTimeMillis() - currentPet.birthday) / DAY_MILLIS).toInt()
            petDao.insertPetHistory(
                PetHistory(
                    speciesId = currentPet.speciesId,
                    name = currentPet.name,
                    personality = currentPet.personality,
                    rarity = currentPet.rarity,
                    level = currentPet.level,
                    daysTogether = daysTogether,
                    memosWritten = 0,
                    departedAt = System.currentTimeMillis()
                )
            )
            petDao.deactivatePet(currentPet.id)
        }
        return hatchPet()
    }

    override suspend fun feedPet(memoCount: Int) {
        val pet = petDao.getActivePet().first() ?: return
        val moodGain = (memoCount * 10).coerceAtMost(30)
        val expGain = memoCount * 20
        val newMood = (pet.mood + moodGain).coerceAtMost(100)
        val newExp = pet.exp + expGain
        val levelUp = newExp >= pet.level * 100
        val finalExp = if (levelUp) newExp - (pet.level * 100) else newExp
        val finalLevel = if (levelUp) pet.level + 1 else pet.level

        petDao.updatePet(
            pet.copy(
                mood = newMood,
                exp = finalExp,
                level = finalLevel,
                lastFedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun interactWithPet() {
        val pet = petDao.getActivePet().first() ?: return
        val newMood = (pet.mood + 5).coerceAtMost(100)
        petDao.updatePet(
            pet.copy(
                mood = newMood,
                lastInteractionAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun updateMood() {
        val pet = petDao.getActivePet().first() ?: return
        val now = System.currentTimeMillis()
        val hoursSinceLastFed = (now - pet.lastFedAt) / HOUR_MILLIS

        val moodDrop = when {
            hoursSinceLastFed >= 48 -> (pet.mood - 20).coerceAtLeast(0)
            hoursSinceLastFed >= 24 -> (pet.mood - 15).coerceAtLeast(0)
            else -> (pet.mood - ((hoursSinceLastFed / 6) * 5).toInt()).coerceAtLeast(0)
        }

        if (moodDrop != pet.mood) {
            petDao.updatePet(pet.copy(mood = moodDrop))
        }
    }

    override suspend fun namePet(name: String) {
        val pet = petDao.getActivePet().first() ?: return
        petDao.updatePet(pet.copy(name = name))
    }

    override fun getPetHistory(): Flow<List<PetHistory>> = petDao.getPetHistory()

    override suspend fun feedPetWithStreakBonus(memoCount: Int, consecutiveDays: Int) {
        val pet = petDao.getActivePet().first() ?: return

        val moodGain = (memoCount * 10).coerceAtMost(30)
        val baseExp = memoCount * 20
        val streakBonus = when {
            consecutiveDays >= 7 -> 100
            consecutiveDays >= 3 -> 50
            else -> 0
        }
        val totalExp = baseExp + streakBonus

        val newMood = (pet.mood + moodGain).coerceAtMost(100)
        val newExp = pet.exp + totalExp
        val levelUp = newExp >= pet.level * 100
        val finalExp = if (levelUp) newExp - (pet.level * 100) else newExp
        val finalLevel = if (levelUp) pet.level + 1 else pet.level

        petDao.updatePet(
            pet.copy(
                mood = newMood,
                exp = finalExp,
                level = finalLevel,
                lastFedAt = System.currentTimeMillis()
            )
        )
    }

    private fun rollRarity(): Int {
        val roll = Random.nextInt(100)
        return when {
            roll < 35 -> 1  // ★☆☆☆☆ 35%
            roll < 65 -> 2  // ★★☆☆☆ 30%
            roll < 85 -> 3  // ★★★☆☆ 20%
            roll < 97 -> 4  // ★★★★☆ 12%
            else -> 5       // ★★★★★ 3%
        }
    }

    companion object {
        private const val DAY_MILLIS = 86_400_000L
        private const val HOUR_MILLIS = 3_600_000L

        private val DISLIKES = listOf(
            "short_memo",
            "no_memo",
            "late_night",
            "long_absence",
            "delete_memo"
        )

        private val CATCHPHRASES = listOf(
            "catchphrase_desu",
            "catchphrase_nya",
            "catchphrase_kya",
            "catchphrase_ung",
            "catchphrase_hehe",
            "catchphrase_hmm",
            "catchphrase_yay"
        )
    }
}
