package me.pecos.nota.presentation.screen.home

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanted.android.wanted.design.theme.DesignSystemTheme
import me.pecos.nota.presentation.components.AppPopup
import me.pecos.nota.presentation.screen.memo.CATEGORY_EMOJIS
import me.pecos.nota.presentation.screen.memo.CATEGORY_RES_IDS
import me.pecos.nota.presentation.screen.home.components.MemoCardItem
import me.pecos.nota.presentation.components.PopupActionArea
import me.pecos.nota.presentation.components.PopupNavigation
import me.pecos.nota.presentation.components.PopupSize
import me.pecos.nota.R
import me.pecos.nota.presentation.screen.home.model.SortOrder
import me.pecos.nota.presentation.theme.LocalAppColors

// ── 홈 화면 ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onDelete: (Int) -> Unit,
    onEdit: (Int) -> Unit,
    viewModel: MainViewModel
) {
    val colors = LocalAppColors.current
    val selectedCategoryIndex by viewModel.selectedCategoryIndex.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val filteredList by viewModel.filteredList.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }
    var tempCategoryIndex by remember(showFilterDialog, selectedCategoryIndex) { mutableIntStateOf(selectedCategoryIndex) }

    val categoryLabels = CATEGORY_RES_IDS.mapIndexed { index, resId ->
        "${CATEGORY_EMOJIS[index]} ${stringResource(resId)}"
    }
    val allLabel = "🗂️ ${stringResource(R.string.category_all)}"
    val currentLabel = if (selectedCategoryIndex == -1) allLabel else categoryLabels[selectedCategoryIndex]

    // ── 카테고리 필터 Popup ────────────────────────────────────────────────────
    if (showFilterDialog) {
        AppPopup(
            onDismissRequest = { showFilterDialog = false },
            title = stringResource(R.string.category_settings),
            navigation = PopupNavigation.EMPHASIZED,
            size = PopupSize.LARGE,
            actionArea = PopupActionArea.NEUTRAL,
            primaryButtonText = stringResource(R.string.save),
            onPrimaryClick = {
                viewModel.setSelectedCategory(tempCategoryIndex)
                showFilterDialog = false
            },
            secondaryButtonText = stringResource(R.string.cancel),
            onSecondaryClick = { showFilterDialog = false }
        ) {
            FlowRow(
                maxItemsInEachRow = 3,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val orderedIndices = listOf(-1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                orderedIndices.forEach { index ->
                    val label = if (index == -1) allLabel else categoryLabels[index]
                    val selected = tempCategoryIndex == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) colors.chipBackground else Color.Transparent)
                            .border(
                                1.dp,
                                if (selected) colors.chipText else colors.cardBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { tempCategoryIndex = index }
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label, maxLines = 1, fontSize = 11.sp,
                            color = if (selected) colors.chipText else colors.textSecondary
                        )
                    }
                }
            }
        }
    }

    // containerColor 명시 → MaterialTheme.colorScheme.surface 무시
    Scaffold(containerColor = colors.screenBackground) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                // 제목 + 메모 개수
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

                // 카테고리 필터 + 정렬 버튼
                Row(
                    modifier = Modifier.padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .border(1.5.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                            .clickable { showFilterDialog = true }
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
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 검색바
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .border(1.5.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 13.sp, color = colors.textSecondary),
                        cursorBrush = SolidColor(colors.textSecondary),
                        decorationBox = { inner ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.search_placeholder),
                                        fontSize = 13.sp,
                                        color = colors.textSecondary.copy(alpha = 0.5f)
                                    )
                                }
                                inner()
                            }
                        }
                    )
                    AnimatedVisibility(
                        visible = searchQuery.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(
                            onClick = { viewModel.setSearchQuery("") },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "검색어 지우기",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn {
                    items(filteredList) { memo ->
                        MemoCardItem(
                            memo = memo,
                            onDelete = { onDelete(memo.id) },
                            onSave = { updatedMemo -> viewModel.updateMemo(updatedMemo) }
                        )
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
}

// Preview removed: HomeScreen requires MainViewModel instance
