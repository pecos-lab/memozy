package me.pecos.memozy.feature.core.resource

import me.pecos.memozy.feature.core.resource.generated.resources.Res
import me.pecos.memozy.feature.core.resource.generated.resources.category_budget
import me.pecos.memozy.feature.core.resource.generated.resources.category_exercise
import me.pecos.memozy.feature.core.resource.generated.resources.category_general
import me.pecos.memozy.feature.core.resource.generated.resources.category_health
import me.pecos.memozy.feature.core.resource.generated.resources.category_idea
import me.pecos.memozy.feature.core.resource.generated.resources.category_schedule
import me.pecos.memozy.feature.core.resource.generated.resources.category_shopping
import me.pecos.memozy.feature.core.resource.generated.resources.category_study
import me.pecos.memozy.feature.core.resource.generated.resources.category_todo
import me.pecos.memozy.feature.core.resource.generated.resources.category_travel
import me.pecos.memozy.feature.core.resource.generated.resources.category_work
import org.jetbrains.compose.resources.StringResource

val CATEGORY_EMOJIS = listOf(
    "📝",
    "💼",
    "💡",
    "✅",
    "📚",
    "📅",
    "💰",
    "🏃",
    "🏥",
    "✈️",
    "🛒",
)

val CATEGORY_RES_IDS: List<StringResource> = listOf(
    Res.string.category_general,
    Res.string.category_work,
    Res.string.category_idea,
    Res.string.category_todo,
    Res.string.category_study,
    Res.string.category_schedule,
    Res.string.category_budget,
    Res.string.category_exercise,
    Res.string.category_health,
    Res.string.category_travel,
    Res.string.category_shopping,
)

val CATEGORY_ALL_TRANSLATIONS = listOf(
    listOf("일반", "General", "一般"),
    listOf("업무", "Work", "仕事"),
    listOf("아이디어", "Idea", "アイデア"),
    listOf("할 일", "To-Do", "やること"),
    listOf("공부", "Study", "勉強"),
    listOf("일정", "Schedule", "予定"),
    listOf("가계부", "Budget", "家計簿"),
    listOf("운동", "Exercise", "運動"),
    listOf("건강", "Health", "健康"),
    listOf("여행", "Travel", "旅行"),
    listOf("쇼핑", "Shopping", "ショッピング"),
)
