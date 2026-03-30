package me.pecos.memozy.feature.memoplain.impl

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.pecos.memozy.data.datasource.local.entity.Memo
import me.pecos.memozy.data.repository.MemoRepository
import me.pecos.memozy.data.repository.model.MemoFormat
import me.pecos.memozy.feature.memoplain.api.MemoPlainNavigation
import me.pecos.memozy.feature.memoplain.api.MemoPlainRoute
import me.pecos.memozy.presentation.screen.home.model.MemoFormatUi
import me.pecos.memozy.presentation.screen.home.model.MemoUiState
import me.pecos.memozy.presentation.screen.memo.MemoScreen
import javax.inject.Inject

class MemoPlainNavigationImpl @Inject constructor(
    private val repository: MemoRepository
) : MemoPlainNavigation {

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        onNavigateToHome: () -> Unit,
        onBack: () -> Unit
    ) {
        navGraphBuilder.composable(MemoPlainRoute.MEMO) { backStackEntry ->
            val memoId = backStackEntry.arguments?.getString("memoId")?.toIntOrNull() ?: -1
            val memos by repository.getMemos()
                .map { list ->
                    list.map { memo ->
                        MemoUiState(
                            id = memo.id,
                            name = memo.name,
                            categoryId = memo.categoryId,
                            content = memo.content,
                            createdAt = memo.createdAt,
                            updatedAt = memo.updatedAt,
                            format = when (memo.format) {
                                MemoFormat.MARKDOWN -> MemoFormatUi.MARKDOWN
                                MemoFormat.PLAIN -> MemoFormatUi.PLAIN
                            }
                        )
                    }
                }
                .collectAsState(initial = emptyList())
            val existingMemo = memos.firstOrNull { it.id == memoId }
            val scope = rememberCoroutineScope()

            MemoScreen(
                existingMemo = existingMemo ?: MemoUiState(0, "", 1, ""),
                onBack = onBack,
                onSave = { memo ->
                    scope.launch {
                        if (memoId > 0 && existingMemo != null) {
                            repository.updateMemo(memo.toEntity())
                        } else {
                            repository.addMemo(memo.toEntity())
                        }
                        onNavigateToHome()
                    }
                }
            )
        }
    }

    private fun MemoUiState.toEntity() = Memo(
        id = id,
        name = name,
        categoryId = categoryId,
        content = content
    )
}
