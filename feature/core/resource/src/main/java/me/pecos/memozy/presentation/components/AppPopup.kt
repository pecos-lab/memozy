package me.pecos.memozy.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.remember
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import me.pecos.memozy.presentation.theme.LocalAppColors
import me.pecos.memozy.presentation.theme.LocalFontSettings

enum class PopupSize { MEDIUM, LARGE, XLARGE }
enum class PopupNavigation { NORMAL, EMPHASIZED, FLOATING }
enum class PopupActionArea { NONE, STRONG, NEUTRAL, COMPACT, CANCEL }

/**
 * 디자인 시스템 Popup 컴포넌트.
 *
 * Navigation:
 *   - NORMAL   : 타이틀 중앙 + X 버튼 오른쪽
 *   - EMPHASIZED: 타이틀 좌측 + X 버튼 오른쪽
 *   - FLOATING : X 버튼만 (타이틀 없음)
 *
 * Size (padding / 콘텐츠 최대 높이):
 *   - MEDIUM  : 20dp / 300dp
 *   - LARGE   : 24dp / 380dp
 *   - XLARGE  : 32dp / 460dp
 *
 * ActionArea:
 *   - NONE    : 버튼 없음
 *   - STRONG  : primary(위) + secondary(아래) 세로 배치
 *   - NEUTRAL : primary + secondary 가로 배치 (동등한 비중)
 *   - COMPACT : 버튼 오른쪽 정렬
 *   - CANCEL  : 단일 닫기 버튼 (전체 너비)
 *
 * ⚠️ X 버튼(Navigation)과 닫기 버튼(CANCEL)을 동시에 사용하지 않도록 설계할 것.
 */
@Composable
fun AppPopup(
    onDismissRequest: () -> Unit,
    title: String? = null,
    navigation: PopupNavigation = PopupNavigation.EMPHASIZED,
    size: PopupSize = PopupSize.MEDIUM,
    actionArea: PopupActionArea = PopupActionArea.NEUTRAL,
    primaryButtonText: String? = null,
    isPrimaryDestructive: Boolean = false,
    onPrimaryClick: (() -> Unit)? = null,
    secondaryButtonText: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalAppColors.current
    val fontSettings = LocalFontSettings.current

    val innerPadding = when (size) {
        PopupSize.MEDIUM -> 20.dp
        PopupSize.LARGE -> 24.dp
        PopupSize.XLARGE -> 32.dp
    }
    val contentMaxHeight = when (size) {
        PopupSize.MEDIUM -> 300.dp
        PopupSize.LARGE -> 380.dp
        PopupSize.XLARGE -> 460.dp
    }
    val primaryColor = if (isPrimaryDestructive) Color(0xFFE24B4A) else colors.chipText

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.cardBackground)
                .padding(innerPadding)
        ) {
            // ── Navigation ────────────────────────────────────────────────────
            when (navigation) {
                PopupNavigation.NORMAL -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (title != null) {
                            Text(
                                text = title,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = fontSettings.scaled(17),
                                color = colors.textTitle,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(horizontal = 32.dp)
                            )
                        }
                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                PopupNavigation.EMPHASIZED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (title != null) {
                            Text(
                                text = title,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = fontSettings.scaled(17),
                                color = colors.textTitle,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                PopupNavigation.FLOATING -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // ── Contents ─────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = contentMaxHeight)
            ) {
                content()
            }

            // ── Action area ───────────────────────────────────────────────────
            if (actionArea != PopupActionArea.NONE) {
                Spacer(modifier = Modifier.height(20.dp))
                when (actionArea) {
                    PopupActionArea.STRONG -> {
                        if (primaryButtonText != null && onPrimaryClick != null) {
                            Button(
                                onClick = onPrimaryClick,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(primaryButtonText, color = Color.White)
                            }
                        }
                        if (secondaryButtonText != null) {
                            TextButton(
                                onClick = onSecondaryClick ?: onDismissRequest,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(secondaryButtonText, color = colors.textSecondary)
                            }
                        }
                    }

                    PopupActionArea.NEUTRAL -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (secondaryButtonText != null) {
                                OutlinedButton(
                                    onClick = onSecondaryClick ?: onDismissRequest,
                                    border = BorderStroke(1.dp, colors.cardBorder),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = colors.textSecondary
                                    )
                                ) {
                                    Text(secondaryButtonText)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            if (primaryButtonText != null && onPrimaryClick != null) {
                                Button(
                                    onClick = onPrimaryClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(primaryButtonText, color = Color.White)
                                }
                            }
                        }
                    }

                    PopupActionArea.COMPACT -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (secondaryButtonText != null) {
                                TextButton(onClick = onSecondaryClick ?: onDismissRequest) {
                                    Text(secondaryButtonText, color = colors.textSecondary)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            if (primaryButtonText != null && onPrimaryClick != null) {
                                Button(
                                    onClick = onPrimaryClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(primaryButtonText, color = Color.White)
                                }
                            }
                        }
                    }

                    PopupActionArea.CANCEL -> {
                        val buttonText = secondaryButtonText ?: primaryButtonText ?: "닫기"
                        val buttonAction = onSecondaryClick ?: onPrimaryClick ?: onDismissRequest
                        TextButton(
                            onClick = buttonAction,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(buttonText, color = colors.chipText)
                        }
                    }

                    PopupActionArea.NONE -> { /* nothing */ }
                }
            }
        }
    }
}
