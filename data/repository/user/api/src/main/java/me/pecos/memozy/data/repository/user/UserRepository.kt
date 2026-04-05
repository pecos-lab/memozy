package me.pecos.memozy.data.repository.user

import kotlinx.coroutines.flow.Flow
import me.pecos.memozy.data.datasource.local.user.entity.Achievement
import me.pecos.memozy.data.datasource.local.user.entity.LearningProgress

interface UserRepository {

    // DataStore - 사용자 설정
    val onboardingCompleted: Flow<Boolean>
    val userLevel: Flow<Int>
    val totalAiChats: Flow<Int>
    val promptsImproved: Flow<Int>

    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setUserLevel(level: Int)
    suspend fun incrementTotalAiChats()
    suspend fun incrementPromptsImproved()

    // Room - 학��� 진행도
    fun getAllProgress(): Flow<List<LearningProgress>>
    fun getCompletedProgress(): Flow<List<LearningProgress>>
    suspend fun getProgressById(guideId: String): LearningProgress?
    suspend fun completeGuide(guideId: String, score: Int? = null)
    suspend fun deleteProgress(guideId: String)

    // Room - 업적
    fun getAllAchievements(): Flow<List<Achievement>>
    suspend fun unlockAchievement(id: String, title: String, description: String)
    suspend fun getAchievementById(id: String): Achievement?
}
