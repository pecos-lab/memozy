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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanted.android.wanted.design.actions.button.WantedButton
import com.wanted.android.wanted.design.actions.button.config.WantedButtonDefaults
import com.wanted.android.wanted.design.util.ButtonType
import com.wanted.android.wanted.design.util.ButtonVariant
import com.wanted.android.wanted.design.theme.DesignSystemTheme
import me.pecos.memozy.presentation.components.AppPopup
import me.pecos.memozy.presentation.screen.home.components.MemoCardItem
import me.pecos.memozy.presentation.components.PopupActionArea
import me.pecos.memozy.presentation.components.PopupNavigation
import me.pecos.memozy.presentation.components.PopupSize
import me.pecos.memozy.feature.core.resource.R
import me.pecos.memozy.presentation.screen.home.model.SortOrder
import me.pecos.memozy.presentation.screen.home.model.TagUiState
import me.pecos.memozy.presentation.theme.LocalAppColors

private val SYSTEM_TAG_NAMES = MainViewModel.SYSTEM_TAGS

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

    // 스와이프 열린 카드 추적 — null이면 모두 닫힌 상태
    var swipedOpenId by remember { mutableStateOf<Int?>(null) }
    val listState = remember(sortOrder) { LazyListState() }
    // 태그 추가 다이얼로그
    var showAddTagDialog by remember { mutableStateOf(false) }
    // 태그 필터 팝업
    var showFilterMenu by remember { mutableStateOf(false) }
    // 태그 편집 BottomSheet
    var tagEditMemoId by remember { mutableStateOf<Int?>(null) }

    // 다중 선택 모드
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Int>()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

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
                    // Row 1: "N개 선택됨" (Memozy 텍스트 자리)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.selected_count, selectedIds.size),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.topbarTitle,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Row 2: [취소] [전체선택] [삭제] — 메인화면 필터 Row와 동일 스타일, 우측 정렬
                    val allSelected = selectedIds.size == filteredList.size && filteredList.isNotEmpty()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Row(
                            modifier = Modifier
                                .border(1.5.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    isSelectionMode = false
                                    selectedIds = emptySet()
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.cancel),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textSecondary
                            )
                        }
                        Row(
                            modifier = Modifier
                                .border(1.5.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedIds = if (allSelected) emptySet()
                                               else filteredList.map { it.id }.toSet()
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (allSelected) stringResource(R.string.deselect_all)
                                        else stringResource(R.string.select_all),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.chipText
                            )
                        }
                        Row(
                            modifier = Modifier
                                .border(1.5.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                                .clickable(enabled = selectedIds.isNotEmpty()) {
                                    showDeleteConfirm = true
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedIds.isNotEmpty())
                                    "${stringResource(R.string.delete_action)} ${selectedIds.size}개"
                                else
                                    stringResource(R.string.delete_action),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (selectedIds.isNotEmpty()) Color(0xFFE24B4A)
                                        else colors.textSecondary
                            )
                        }
                    }
                }

                // 태그 필터 버튼 + 정렬 버튼 — 선택 모드에서 숨김
                if (!isSelectionMode) {
                val currentFilterLabel = when (selectedTagId) {
                    -1 -> stringResource(R.string.filter_all)
                    else -> allTags.firstOrNull { it.id == selectedTagId }?.name
                        ?: stringResource(R.string.filter_all)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 태그 필터 버튼 (AppPopup 트리거)
                    Row(
                        modifier = Modifier
                            .border(1.5.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                            .clickable { showFilterMenu = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = currentFilterLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // 정렬 버튼
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
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        )
                    }

                    // 선택 버튼
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
                                // 왼쪽: 체크 점
                                Box(
                                    modifier = Modifier.width(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.CheckCircle
                                                      else Icons.Outlined.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isSelected) Color(0xFF2196F3)
                                               else colors.textSecondary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                // 오른쪽: 카드 (살짝 밀린 상태)
                                Box(modifier = Modifier.weight(1f)) {
                                    MemoCardItem(memo = memo, tags = memoTags[memo.id] ?: emptyList())
                                }
                            }
                        } else {
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
                                Row(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
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
                                    MemoCardItem(
                                        memo = memo,
                                        tags = memoTags[memo.id] ?: emptyList(),
                                        onTagsClick = { tagEditMemoId = memo.id }
                                    )
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

    // 태그 추가 다이얼로그 (AppPopup 사용)
    if (showAddTagDialog) {
        var tagName by remember { mutableStateOf("") }
        AppPopup(
            onDismissRequest = { showAddTagDialog = false; tagName = "" },
            title = stringResource(R.string.add_tag),
            navigation = PopupNavigation.EMPHASIZED,
            size = PopupSize.MEDIUM,
            actionArea = PopupActionArea.NEUTRAL,
            primaryButtonText = stringResource(R.string.add),
            onPrimaryClick = {
                if (tagName.isNotBlank()) {
                    viewModel.createTag(tagName.trim())
                    showAddTagDialog = false
                    tagName = ""
                }
            },
            secondaryButtonText = stringResource(R.string.cancel),
            onSecondaryClick = { showAddTagDialog = false; tagName = "" }
        ) {
            val colors2 = LocalAppColors.current
            val focusRequester = remember { FocusRequester() }
            BasicTextField(
                value = tagName,
                onValueChange = { tagName = it },
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp, color = colors2.textTitle),
                cursorBrush = SolidColor(colors2.chipText),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (tagName.isNotBlank()) {
                        viewModel.createTag(tagName.trim())
                        showAddTagDialog = false
                        tagName = ""
                    }
                }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors2.cardBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        if (tagName.isEmpty()) {
                            Text(
                                text = stringResource(R.string.tag_name),
                                fontSize = 14.sp,
                                color = colors2.textSecondary
                            )
                        }
                        innerTextField()
                    }
                }
            )
            LaunchedEffect(Unit) { focusRequester.requestFocus() }
        }
    }

    // 태그 필터 선택 팝업 (AppPopup)
    if (showFilterMenu) {
        TagFilterPopup(
            selectedTagId = selectedTagId,
            allTags = allTags,
            onSelectTag = { tagId ->
                viewModel.setSelectedTag(tagId)
                showFilterMenu = false
            },
            onAddTag = {
                showFilterMenu = false
                showAddTagDialog = true
            },
            onDismiss = { showFilterMenu = false }
        )
    }

    // 태그 편집 BottomSheet
    tagEditMemoId?.let { memoId ->
        TagEditBottomSheet(
            memoId = memoId,
            currentTags = memoTags[memoId] ?: emptyList(),
            allTags = allTags,
            onAddTag = { tagId -> viewModel.addTagToMemo(memoId, tagId) },
            onRemoveTag = { tagId -> viewModel.removeTagFromMemo(memoId, tagId) },
            onCreateTag = { name -> viewModel.createTag(name) },
            onDismiss = { tagEditMemoId = null }
        )
    }
}

// ── 태그 필터 팝업 ─────────────────────────────────────────────────────────────

@Composable
private fun TagFilterPopup(
    selectedTagId: Int,
    allTags: List<TagUiState>,
    onSelectTag: (Int) -> Unit,
    onAddTag: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current

    AppPopup(
        onDismissRequest = onDismiss,
        title = "태그 필터",
        navigation = PopupNavigation.EMPHASIZED,
        size = PopupSize.LARGE,
        actionArea = PopupActionArea.NONE
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // 전체
            TagFilterRow(
                label = "전체",
                selected = selectedTagId == -1,
                onClick = { onSelectTag(-1) }
            )
            allTags.forEach { tag ->
                HorizontalDivider(color = colors.cardBorder)
                TagFilterRow(
                    label = tag.name,
                    selected = selectedTagId == tag.id,
                    onClick = { onSelectTag(tag.id) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            WantedButton(
                text = "+ 새 태그 만들기",
                modifier = Modifier.fillMaxWidth(),
                buttonDefault = WantedButtonDefaults.getDefault(
                    type = ButtonType.ASSISTIVE,
                    variant = ButtonVariant.OUTLINED
                ).copy(contentColor = colors.textSecondary),
                onClick = onAddTag
            )
        }
    }
}

@Composable
private fun TagFilterRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) colors.chipText else colors.textTitle,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = colors.chipText,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ── 태그 편집 바텀시트 ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TagEditBottomSheet(
    memoId: Int,
    currentTags: List<TagUiState>,
    allTags: List<TagUiState>,
    onAddTag: (Int) -> Unit,
    onRemoveTag: (Int) -> Unit,
    onCreateTag: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = LocalAppColors.current
    var showCreateTagField by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.cardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp)
        ) {
            // 헤더
            Text(
                text = "태그 편집",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = colors.textTitle
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 현재 태그
            if (currentTags.isNotEmpty()) {
                Text("현재 태그", fontSize = 11.sp, color = colors.textSecondary, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    currentTags.forEach { tag ->
                        val isSystem = tag.name in SYSTEM_TAG_NAMES
                        Row(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .background(colors.chipBackground, RoundedCornerShape(50))
                                .padding(
                                    start = 10.dp,
                                    end = if (isSystem) 10.dp else 6.dp,
                                    top = 5.dp,
                                    bottom = 5.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tag.name, fontSize = 13.sp, color = colors.chipText, fontWeight = FontWeight.Medium)
                            if (!isSystem) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "제거",
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { onRemoveTag(tag.id) },
                                    tint = colors.chipText.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = colors.cardBorder)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 추가 가능한 태그
            val availableTags = allTags.filter { all -> currentTags.none { it.id == all.id } }
            if (availableTags.isNotEmpty()) {
                Text("태그 추가", fontSize = 11.sp, color = colors.textSecondary, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableTags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .border(1.dp, colors.cardBorder, RoundedCornerShape(50))
                                .clickable { onAddTag(tag.id) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text("+ ${tag.name}", fontSize = 13.sp, color = colors.textSecondary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 새 태그 생성
            if (showCreateTagField) {
                Spacer(modifier = Modifier.height(4.dp))
                val focusRequester = remember { FocusRequester() }
                BasicTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 14.sp, color = colors.textTitle),
                    cursorBrush = SolidColor(colors.chipText),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (newTagName.isNotBlank()) {
                            onCreateTag(newTagName.trim())
                            newTagName = ""
                            showCreateTagField = false
                        }
                    }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (newTagName.isEmpty()) {
                                    Text("태그 이름", fontSize = 14.sp, color = colors.textSecondary)
                                }
                                innerTextField()
                            }
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "추가",
                                tint = if (newTagName.isNotBlank()) colors.chipText else colors.textSecondary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable(enabled = newTagName.isNotBlank()) {
                                        onCreateTag(newTagName.trim())
                                        newTagName = ""
                                        showCreateTagField = false
                                    }
                            )
                        }
                    }
                )
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                WantedButton(
                    text = "+ 새 태그 만들기",
                    modifier = Modifier.fillMaxWidth(),
                    buttonDefault = WantedButtonDefaults.getDefault(
                        type = ButtonType.ASSISTIVE,
                        variant = ButtonVariant.OUTLINED
                    ).copy(contentColor = colors.textSecondary),
                    onClick = { showCreateTagField = true }
                )
            }
        }
    }
}
