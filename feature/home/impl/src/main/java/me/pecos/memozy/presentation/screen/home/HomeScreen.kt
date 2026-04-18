package me.pecos.memozy.presentation.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.pecos.memozy.presentation.components.AppPopup
import me.pecos.memozy.presentation.screen.home.components.MemoCardItem
import me.pecos.memozy.presentation.components.PopupActionArea
import me.pecos.memozy.presentation.components.PopupNavigation
import me.pecos.memozy.presentation.components.PopupSize
import me.pecos.memozy.feature.core.resource.R
import me.pecos.memozy.feature.core.viewmodel.MainViewModel
import me.pecos.memozy.feature.core.viewmodel.model.HomeUiState
import me.pecos.memozy.feature.core.viewmodel.model.SortOrder
import me.pecos.memozy.presentation.theme.LocalAppColors
import me.pecos.memozy.presentation.theme.LocalFontSettings

// ── 홈 화면 ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onDelete: (Int) -> Unit,
    onEdit: (Int) -> Unit,
    viewModel: MainViewModel
) {
    val colors = LocalAppColors.current
    val fontSettings = LocalFontSettings.current
    val homeUiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val filteredList by viewModel.filteredList.collectAsState()

    val listState = remember(sortOrder) { LazyListState() }

    // 다중 선택 모드
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Int>()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = colors.screenBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) { }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                // 제목 + 메모 개수 (항상 표시)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSelectionMode) stringResource(R.string.selected_count, selectedIds.size) else "Memozy",
                        fontSize = fontSettings.scaled(22),
                        fontWeight = FontWeight.Bold,
                        color = colors.topbarTitle,
                        modifier = Modifier.weight(1f)
                    )
                    if (!isSelectionMode) {
                        Text(
                            text = stringResource(R.string.memo_count, filteredList.size),
                            fontSize = fontSettings.scaled(12),
                            color = colors.textSecondary
                        )
                    }
                }

                // 태그 필터 버튼 / 선택 모드 액션 버튼
                if (isSelectionMode) {
                    val allSelected = selectedIds.size == filteredList.size && filteredList.isNotEmpty()
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        // 전체선택
                        Text(
                            text = if (allSelected) stringResource(R.string.deselect_all)
                                    else stringResource(R.string.select_all),
                            fontSize = fontSettings.scaled(13),
                            fontWeight = FontWeight.Medium,
                            color = colors.chipText,
                            modifier = Modifier
                                .border(1.5.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedIds = if (allSelected) emptySet()
                                                 else filteredList.map { it.id }.toSet()
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                        // 고정
                        Text(
                            text = stringResource(R.string.pin),
                            fontSize = fontSettings.scaled(13),
                            fontWeight = FontWeight.Medium,
                            color = if (selectedIds.isNotEmpty()) Color(0xFFFFA726) else colors.textSecondary,
                            modifier = Modifier
                                .border(1.5.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                                .clickable(enabled = selectedIds.isNotEmpty()) {
                                    val allPinned = filteredList.filter { it.id in selectedIds }.all { it.isPinned }
                                    viewModel.pinMemos(selectedIds, !allPinned)
                                    isSelectionMode = false
                                    selectedIds = emptySet()
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                        // 삭제
                        Text(
                            text = if (selectedIds.isNotEmpty())
                                "${stringResource(R.string.delete_action)} ${selectedIds.size}"
                            else
                                stringResource(R.string.delete_action),
                            fontSize = fontSettings.scaled(13),
                            fontWeight = FontWeight.Medium,
                            color = if (selectedIds.isNotEmpty()) Color(0xFFE24B4A) else colors.textSecondary,
                            modifier = Modifier
                                .border(1.5.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                                .clickable(enabled = selectedIds.isNotEmpty()) {
                                    showDeleteConfirm = true
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                        // 취소
                        Text(
                            text = stringResource(R.string.cancel),
                            fontSize = fontSettings.scaled(13),
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary,
                            modifier = Modifier
                                .border(1.5.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    isSelectionMode = false
                                    selectedIds = emptySet()
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Row(
                            modifier = Modifier
                                .border(1.5.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                                .clickable { viewModel.toggleSortOrder() }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (sortOrder == SortOrder.NEWEST)
                                    stringResource(R.string.sort_newest)
                                else
                                    stringResource(R.string.sort_oldest),
                                fontSize = fontSettings.scaled(13),
                                fontWeight = FontWeight.Medium,
                                color = colors.textSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(
                            modifier = Modifier
                                .border(1.5.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                                .clickable { isSelectionMode = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.edit_mode),
                                fontSize = fontSettings.scaled(13),
                                fontWeight = FontWeight.Medium,
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(filteredList, key = { it.id }) { memo ->
                        val itemModifier = Modifier.animateItem(
                            fadeInSpec = tween(250),
                            fadeOutSpec = tween(250),
                            placementSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        )

                        val isSelected = isSelectionMode && memo.id in selectedIds
                        val cardInteractionSource = remember { MutableInteractionSource() }
                        val isPressed by cardInteractionSource.collectIsPressedAsState()
                        val cardScale by animateFloatAsState(
                            targetValue = if (isPressed) 0.97f else 1f,
                            animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
                            label = "cardScale"
                        )

                        Row(
                            modifier = itemModifier
                                .fillMaxWidth()
                                .graphicsLayer(scaleX = cardScale, scaleY = cardScale)
                                .combinedClickable(
                                    interactionSource = cardInteractionSource,
                                    indication = null,
                                    onClick = {
                                        if (isSelectionMode) {
                                            selectedIds = if (memo.id in selectedIds) selectedIds - memo.id
                                                          else selectedIds + memo.id
                                            if (selectedIds.isEmpty()) isSelectionMode = false
                                        } else {
                                            onEdit(memo.id)
                                        }
                                    },
                                    onLongClick = {
                                        if (!isSelectionMode) {
                                            isSelectionMode = true
                                            selectedIds = setOf(memo.id)
                                        }
                                    }
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSelectionMode) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle
                                                  else Icons.Outlined.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFF2196F3)
                                           else colors.textSecondary.copy(alpha = 0.4f),
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .size(22.dp)
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                MemoCardItem(
                                    memo = memo,
                                    isInSelectionMode = isSelectionMode
                                )
                            }
                        }
                    }
                }
            }
            when {
                homeUiState is HomeUiState.Loading -> {
                    // 빈 배경만 표시 — Room 로딩이 빨라서 깜빡임 방지
                }
                homeUiState is HomeUiState.Empty -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_full),
                            contentDescription = null,
                            modifier = Modifier.size(160.dp),
                            alpha = if (isSystemInDarkTheme()) 0.7f else 0.15f
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.empty_memo_hint),
                            fontSize = fontSettings.scaled(14),
                            color = colors.textSecondary.copy(alpha = if (isSystemInDarkTheme()) 0.75f else 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // 다중 선택 삭제 확인 다이얼로그
    if (showDeleteConfirm) {
        AppPopup(
            onDismissRequest = { showDeleteConfirm = false },
            title = stringResource(R.string.delete_selected_title),
            navigation = PopupNavigation.EMPHASIZED,
            size = PopupSize.MEDIUM,
            actionArea = PopupActionArea.NEUTRAL,
            primaryButtonText = stringResource(R.string.delete_action),
            isPrimaryDestructive = true,
            onPrimaryClick = {
                viewModel.deleteMemos(selectedIds)
                showDeleteConfirm = false
                isSelectionMode = false
                selectedIds = emptySet()
            },
            secondaryButtonText = stringResource(R.string.cancel),
            onSecondaryClick = { showDeleteConfirm = false }
        ) {
            Text(
                stringResource(R.string.delete_selected_message, selectedIds.size),
                color = LocalAppColors.current.textBody
            )
        }
    }

}
