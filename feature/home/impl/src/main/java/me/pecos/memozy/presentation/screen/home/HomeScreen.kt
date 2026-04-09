package me.pecos.memozy.presentation.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanted.android.wanted.design.theme.DesignSystemTheme
import me.pecos.memozy.presentation.components.AppPopup
import me.pecos.memozy.feature.core.resource.CATEGORY_EMOJIS
import me.pecos.memozy.feature.core.resource.CATEGORY_RES_IDS
import me.pecos.memozy.presentation.screen.home.components.MemoCardItem
import me.pecos.memozy.presentation.components.PopupActionArea
import me.pecos.memozy.presentation.components.PopupNavigation
import me.pecos.memozy.presentation.components.PopupSize
import me.pecos.memozy.feature.core.resource.R
import me.pecos.memozy.presentation.screen.home.model.SortOrder
import me.pecos.memozy.presentation.theme.LocalAppColors

// ── 홈 화면 ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onDelete: (Int) -> Unit,
    onEdit: (Int) -> Unit,
    viewModel: MainViewModel
) {
    val colors = LocalAppColors.current
    val selectedTagId by viewModel.selectedTagId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val filteredList by viewModel.filteredList.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    val memoTags by viewModel.memoTags.collectAsState()
    // 하위호환
    val selectedCategoryIndex = selectedTagId

    // 메모 태그 로드
    LaunchedEffect(filteredList) {
        viewModel.loadMemoTags(filteredList.map { it.id })
    }

    // 스와이프 열린 카드 추적 — null이면 모두 닫힌 상태
    var swipedOpenId by remember { mutableStateOf<Int?>(null) }
    val listState = remember(sortOrder) { LazyListState() }
    // 태그 추가 다이얼로그
    var showAddTagDialog by remember { mutableStateOf(false) }

    // 다중 선택 모드
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Int>()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // 자동 뷰 필터 타입
    // -1 = 전체, -2 = 일반메모, -3 = 유튜브, -4 = 녹음, 양수 = 사용자 태그 ID

    // containerColor 명시 → MaterialTheme.colorScheme.surface 무시
    Scaffold(containerColor = colors.screenBackground) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) { swipedOpenId = null }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp)
            ) {
                // 제목 + 메모 개수 / 선택 모드 헤더
                if (!isSelectionMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Memozy",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.topbarTitle,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = stringResource(R.string.memo_count, filteredList.size),
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.chipText,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    isSelectionMode = false
                                    selectedIds = emptySet()
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.selected_count, selectedIds.size),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.topbarTitle,
                            modifier = Modifier.weight(1f)
                        )
                        val allSelected = selectedIds.size == filteredList.size && filteredList.isNotEmpty()
                        Text(
                            text = if (allSelected) stringResource(R.string.deselect_all) else stringResource(R.string.select_all),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.chipText,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedIds = if (allSelected) emptySet()
                                                  else filteredList.map { it.id }.toSet()
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.delete_action),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedIds.isNotEmpty()) Color(0xFFE24B4A) else colors.textSecondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = selectedIds.isNotEmpty()) {
                                    showDeleteConfirm = true
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }

                // 태그 필터 (드롭다운) + 정렬 버튼 — 선택 모드에서 숨김
                if (!isSelectionMode) {
                var showFilterMenu by remember { mutableStateOf(false) }
                val filterAllLabel = stringResource(R.string.filter_all)
                val autoViews = listOf(
                    -1 to filterAllLabel,
                    -2 to stringResource(R.string.filter_memo),
                    -3 to stringResource(R.string.filter_youtube),
                    -4 to stringResource(R.string.filter_recording)
                )
                val currentLabel = autoViews.firstOrNull { it.first == selectedTagId }?.second
                    ?: allTags.firstOrNull { it.id == selectedTagId }?.name
                    ?: filterAllLabel

                Row(
                    modifier = Modifier.padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Row(
                            modifier = Modifier
                                .border(1.5.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                                .clickable { showFilterMenu = true }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        androidx.compose.material3.DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            // 자동 뷰
                            autoViews.forEach { (id, label) ->
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text(label, fontWeight = if (selectedTagId == id) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = { viewModel.setSelectedTag(id); showFilterMenu = false }
                                )
                            }
                            if (allTags.isNotEmpty()) {
                                androidx.compose.material3.HorizontalDivider()
                                // 사용자 태그
                                allTags.forEach { tag ->
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text(tag.name, fontWeight = if (selectedTagId == tag.id) FontWeight.Bold else FontWeight.Normal) },
                                        onClick = { viewModel.setSelectedTag(tag.id); showFilterMenu = false }
                                    )
                                }
                            }
                            androidx.compose.material3.HorizontalDivider()
                            // 태그 추가
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text("+ 태그 추가", color = colors.chipText) },
                                onClick = { showFilterMenu = false; showAddTagDialog = true }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        modifier = Modifier
                            .border(1.5.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                            .clickable { viewModel.toggleSortOrder() }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (sortOrder == SortOrder.NEWEST)
                                stringResource(R.string.sort_newest)
                            else
                                stringResource(R.string.sort_oldest),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.select),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary,
                        modifier = Modifier
                            .border(1.5.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                isSelectionMode = true
                                swipedOpenId = null
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
                } // if (!isSelectionMode)

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredList, key = { it.id }) { memo ->
                        val itemModifier = Modifier.animateItem(
                            fadeInSpec = tween(300),
                            fadeOutSpec = tween(300),
                            placementSpec = tween(300)
                        )

                        if (isSelectionMode) {
                            // ── 선택 모드: 체크 + 카드 (스와이프 없음) ──
                            val isSelected = memo.id in selectedIds
                            Row(
                                modifier = itemModifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedIds = if (isSelected) selectedIds - memo.id
                                                      else selectedIds + memo.id
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.width(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.CheckCircle
                                                      else Icons.Outlined.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isSelected) Color(0xFF2196F3) else colors.textSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    MemoCardItem(memo = memo, tags = memoTags[memo.id] ?: emptyList())
                                }
                            }
                        } else {
                            // ── 일반 모드: 스와이프 (기존 로직) ──
                            val revealWidth = 80f
                            val revealPx = with(androidx.compose.ui.platform.LocalDensity.current) { revealWidth.dp.toPx() }
                            val isOpen = swipedOpenId == memo.id
                            var rawOffset by remember { mutableStateOf(0f) }
                            val offsetX = if (isOpen) rawOffset else 0f
                            val animatedOffset by animateFloatAsState(
                                targetValue = offsetX,
                                animationSpec = tween(200),
                                label = "swipeOffset"
                            )

                            Box(modifier = itemModifier.fillMaxWidth()) {
                                // 뒤쪽 액션 버튼
                                Row(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // 핀 토글
                                    Box(
                                        modifier = Modifier
                                            .width(revealWidth.dp)
                                            .fillMaxHeight()
                                            .background(if (memo.isPinned) colors.textSecondary else Color(0xFFFFA726))
                                            .clickable {
                                                swipedOpenId = null
                                                viewModel.togglePin(memo)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                            Text(
                                                text = if (memo.isPinned) stringResource(R.string.unpin) else stringResource(R.string.pin),
                                                color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                    // 삭제
                                    Box(
                                        modifier = Modifier
                                            .width(revealWidth.dp)
                                            .fillMaxHeight()
                                            .background(Color(0xFFE24B4A))
                                            .clickable {
                                                swipedOpenId = null
                                                onDelete(memo.id)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                            Text(
                                                text = stringResource(R.string.delete_memo),
                                                color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                // 앞쪽 카드
                                val dragState = rememberDraggableState { delta ->
                                    rawOffset = (rawOffset + delta).coerceIn(-revealPx, revealPx)
                                    swipedOpenId = memo.id
                                }
                                Box(
                                    modifier = Modifier
                                        .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                                        .draggable(
                                            state = dragState,
                                            orientation = Orientation.Horizontal,
                                            onDragStopped = {
                                                val snapped = when {
                                                    rawOffset > revealPx / 2 -> revealPx
                                                    rawOffset < -revealPx / 2 -> -revealPx
                                                    else -> 0f
                                                }
                                                rawOffset = snapped
                                                swipedOpenId = if (snapped != 0f) memo.id else null
                                            }
                                        )
                                        .clickable {
                                            if (swipedOpenId != null) {
                                                swipedOpenId = null
                                            } else {
                                                onEdit(memo.id)
                                            }
                                        }
                                ) {
                                    MemoCardItem(memo = memo, tags = memoTags[memo.id] ?: emptyList())
                                }
                            }
                        }
                    }
                }
            }
            if (filteredList.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_full),
                        contentDescription = null,
                        modifier = Modifier.size(160.dp),
                        alpha = 0.15f
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isNotBlank())
                            stringResource(R.string.empty_search_hint)
                        else
                            stringResource(R.string.empty_memo_hint),
                        fontSize = 14.sp,
                        color = colors.textSecondary.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // 다중 선택 삭제 확인 다이얼로그
    if (showDeleteConfirm) {
        me.pecos.memozy.presentation.components.AppPopup(
            onDismissRequest = { showDeleteConfirm = false },
            title = stringResource(R.string.delete_selected_title),
            navigation = me.pecos.memozy.presentation.components.PopupNavigation.EMPHASIZED,
            size = me.pecos.memozy.presentation.components.PopupSize.MEDIUM,
            actionArea = me.pecos.memozy.presentation.components.PopupActionArea.NEUTRAL,
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
                color = colors.textBody
            )
        }
    }

    // 태그 추가 다이얼로그
    if (showAddTagDialog) {
        var tagName by remember { mutableStateOf("") }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddTagDialog = false },
            title = { Text(stringResource(R.string.add_tag), fontWeight = FontWeight.Bold) },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    placeholder = { Text(stringResource(R.string.tag_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        if (tagName.isNotBlank()) {
                            viewModel.createTag(tagName.trim())
                            showAddTagDialog = false
                        }
                    }
                ) { Text(stringResource(R.string.add)) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showAddTagDialog = false }
                ) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

// Preview removed: HomeScreen requires MainViewModel instance
