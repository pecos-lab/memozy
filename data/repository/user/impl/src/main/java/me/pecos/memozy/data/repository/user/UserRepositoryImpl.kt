package me.pecos.memozy.data.repository.user

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.pecos.memozy.data.datasource.local.user.AchievementDao
import me.pecos.memozy.data.datasource.local.user.LearningProgressDao
import me.pecos.memozy.data.datasource.local.user.UserPrefs
import me.pecos.memozy.data.datasource.local.user.entity.Achievement
import me.pecos.memozy.data.datasource.local.user.entity.LearningProgress
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val learningProgressDao: LearningProgressDao,
    private val achievementDao: AchievementDao
) : UserRepository {

    // DataStore - 사용자 설정
    override val onboardingCompleted: Flow<Boolean> =
        dataStore.data.map { it[UserPrefs.ONBOARDING_COMPLETED] ?: false }

    override val userLevel: Flow<Int> =
        dataStore.data.map { it[UserPrefs.USER_LEVEL] ?: 1 }

    override val totalAiChats: Flow<Int> =
        dataStore.data.map { it[UserPrefs.TOTAL_AI_CHATS] ?: 0 }

    override val promptsImproved: Flow<Int> =
        dataStore.data.map { it[UserPrefs.PROMPTS_IMPROVED] ?: 0 }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[UserPrefs.ONBOARDING_COMPLETED] = completed }
    }

    override suspend fun setUserLevel(level: Int) {
        dataStore.edit { it[UserPrefs.USER_LEVEL] = level }
    }

    override suspend fun incrementTotalAiChats() {
        dataStore.edit { prefs ->
            val current = prefs[UserPrefs.TOTAL_AI_CHATS] ?: 0
            prefs[UserPrefs.TOTAL_AI_CHATS] = current + 1
        }
    }

    override suspend fun incrementPromptsImproved() {
        dataStore.edit { prefs ->
            val current = prefs[UserPrefs.PROMPTS_IMPROVED] ?: 0
            prefs[UserPrefs.PROMPTS_IMPROVED] = current + 1
        }
    }

    // Room - 학습 진행도
    override fun getAllProgress(): Flow<List<LearningProgress>> =
        learningProgressDao.getAllProgress()

    override fun getCompletedProgress(): Flow<List<LearningProgress>> =
        learningProgressDao.getCompletedProgress()

    override suspend fun getProgressById(guideId: String): LearningProgress? =
        learningProgressDao.getProgressById(guideId)

    override suspend fun completeGuide(guideId: String, score: Int?) {
        learningProgressDao.upsertProgress(
            LearningProgress(
                guideId = guideId,
                completed = true,
                completedAt = System.currentTimeMillis(),
                score = score
            )
        )
    }

    override suspend fun deleteProgress(guideId: String) {
        learningProgressDao.deleteProgressById(guideId)
    }

    // Room - 업적
    override fun getAllAchievements(): Flow<List<Achievement>> =
        achievementDao.getAllAchievements()

    override suspend fun unlockAchievement(id: String, title: String, description: String) {
        achievementDao.upsertAchievement(
            Achievement(
                id = id,
                unlockedAt = System.currentTimeMillis(),
                title = title,
                description = description
            )
        )
    }

    override suspend fun getAchievementById(id: String): Achievement? =
        achievementDao.getAchievementById(id)
}
