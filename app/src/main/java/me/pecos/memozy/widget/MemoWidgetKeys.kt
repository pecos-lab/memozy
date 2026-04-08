package me.pecos.memozy.widget

import androidx.glance.action.ActionParameters

object MemoWidgetKeys {
    const val ACTION_NEW_MEMO = "new_memo"
    const val ACTION_OPEN_MEMO = "open_memo"

    val ACTION_KEY = ActionParameters.Key<String>("widget_action")
    val MEMO_ID_KEY = ActionParameters.Key<Int>("widget_memo_id")
}
