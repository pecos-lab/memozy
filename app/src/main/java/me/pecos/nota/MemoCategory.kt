package me.pecos.nota

import androidx.annotation.StringRes

enum class MemoCategory(val code: Int, @StringRes val labelResId: Int) {
    GENERAL(0, R.string.category_general),
    WORK(1, R.string.category_work),
    IDEA(2, R.string.category_idea),
    TODO(3, R.string.category_todo),
    STUDY(4, R.string.category_study),
    PERSONAL(5, R.string.category_personal),
    SCHEDULE(6, R.string.category_schedule),
    BUDGET(7, R.string.category_budget);

    companion object {
        fun fromCode(code: Int): MemoCategory = entries.find { it.code == code } ?: GENERAL
    }
}
