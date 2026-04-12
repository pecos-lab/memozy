package me.pecos.memozy.presentation.screen.trash

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.pecos.memozy.feature.core.resource.R
import me.pecos.memozy.presentation.components.AppPopup
import me.pecos.memozy.presentation.components.PopupActionArea
import me.pecos.memozy.presentation.components.PopupNavigation
import me.pecos.memozy.presentation.components.PopupSize
import me.pecos.memozy.presentation.screen.home.model.MemoUiState
import me.pecos.memozy.presentation.theme.LocalAppColors
import me.pecos.memozy.presentation.theme.LocalFontSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun TrashScreen(
    viewModel: TrashViewModel,
    onBack: () -> Unit
) {
    val deletedMemos by viewModel.deletedMemos.collectAsState()
    val colors = LocalAppColors.current
    val fontSettings = LocalFontSettings.current
    var showEmptyDialog by remember { mutableStateOf(false) }

    if (showEmptyDialog) {
        AppPopup(
            onDismissRequest = { showEmptyDialog = false },
            title = stringResource(R.string.trash_empty_confirm_title),
            navigation = PopupNavigation.EMPHASIZED,
            size = PopupSize.MEDIUM,
            actionArea = PopupActionArea.NEUTRAL,
            primaryButtonText = stringResource(R.string.trash_empty_action),
            isPrimaryDestructive = true,
            onPrimaryClick = {
                viewModel.emptyTrash()
                showEmptyDialog = false
            },
            secondaryButtonText = stringResource(R.string.cancel),
            onSecondaryClick = { showEmptyDialog = false }
        ) {
            Text(
                stringResource(R.string.trash_empty_confirm_message),
                color = colors.textBody
            )
        }
    }

    Scaffold(containerColor = colors.screenBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = colors.topbarTitle
                    )
                }
                Text(
                    text = stringResource(R.string.trash_title),
                    fontSize = fontSettings.scaled(22),
                    fontWeight = FontWeight.Bold,
                    color = colors.topbarTitle,
                    modifier = Modifier.weight(1f)
                )
                if (deletedMemos.isNotEmpty()) {
                    TextButton(onClick = { showEmptyDialog = true }) {
                        Text(
                            stringResource(R.string.trash_empty_action),
                            color = colors.actionDeleteTint,
                            fontSize = fontSettings.scaled(14)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.trash_auto_delete_hint),
                fontSize = fontSettings.scaled(12),
                color = colors.textSecondary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (deletedMemos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.trash_empty),
                        color = colors.textSecondary,
                        fontSize = fontSettings.scaled(14)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(deletedMemos, key = { it.id }) { memo ->
                        TrashMemoItem(
                            memo = memo,
                            onRestore = { viewModel.restoreMemo(memo.id) },
                            onDelete = { viewModel.permanentlyDeleteMemo(memo.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrashMemoItem(
    memo: MemoUiState,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalAppColors.current
    val fontSettings = LocalFontSettings.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AppPopup(
            onDismissRequest = { showDeleteDialog = false },
            title = stringResource(R.string.trash_permanent_delete_title),
            navigation = PopupNavigation.EMPHASIZED,
            size = PopupSize.MEDIUM,
            actionArea = PopupActionArea.NEUTRAL,
            primaryButtonText = stringResource(R.string.trash_permanent_delete_action),
            isPrimaryDestructive = true,
            onPrimaryClick = {
                onDelete()
                showDeleteDialog = false
            },
            secondaryButtonText = stringResource(R.string.cancel),
            onSecondaryClick = { showDeleteDialog = false }
        ) {
            Text(
                stringResource(R.string.trash_permanent_delete_message),
                color = colors.textBody
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.cardBackground)
            .padding(16.dp)
    ) {
        Text(
            text = memo.name.ifBlank { stringResource(R.string.memo_title_placeholder) },
            fontSize = fontSettings.scaled(16),
            fontWeight = FontWeight.Medium,
            color = colors.textTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (memo.content.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = memo.content
                    .replace(Regex("<br\\s*/?>"), "\n")
                    .replace(Regex("<[^>]+>"), "")
                    .replace("&nbsp;", " ")
                    .replace("&amp;", "&")
                    .trim()
                    .take(100),
                fontSize = fontSettings.scaled(13),
                color = colors.textBody,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatDeletedTime(memo.deletedAt),
                fontSize = fontSettings.scaled(11),
                color = colors.textSecondary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.trash_restore),
                    fontSize = fontSettings.scaled(12),
                    fontWeight = FontWeight.Medium,
                    color = colors.actionEditTint,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onRestore)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Text(
                    text = stringResource(R.string.trash_permanent_delete_action),
                    fontSize = fontSettings.scaled(12),
                    fontWeight = FontWeight.Medium,
                    color = colors.actionDeleteTint,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = { showDeleteDialog = true })
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun formatDeletedTime(deletedAt: Long?): String {
    if (deletedAt == null) return ""
    val now = System.currentTimeMillis()
    val daysLeft = 30 - TimeUnit.MILLISECONDS.toDays(now - deletedAt)
    return if (daysLeft > 0) {
        stringResource(R.string.trash_days_left, daysLeft.toInt())
    } else {
        stringResource(R.string.trash_soon_delete)
    }
}
