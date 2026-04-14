package me.pecos.memozy.presentation.screen.memo.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.pecos.memozy.presentation.theme.AppColors
import me.pecos.memozy.presentation.theme.LocalAppColors
import me.pecos.memozy.presentation.theme.LocalFontSettings

data class AiAssistMessage(
    val role: String, // "user" or "assistant"
    val content: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistBottomSheet(
    messages: List<AiAssistMessage>,
    streamingText: String?,
    isLoading: Boolean,
    onSendMessage: (String) -> Unit,
    onInsertToMemo: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    val fontSettings = LocalFontSettings.current
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // 새 메시지가 추가되면 자동 스크롤
    val totalItems = messages.size + if (streamingText != null) 1 else 0
    LaunchedEffect(totalItems, streamingText) {
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.cardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 300.dp, max = 500.dp)
                .imePadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            // 헤더
            Text(
                text = "AI 어시스트",
                fontSize = fontSettings.scaled(16),
                fontWeight = FontWeight.Bold,
                color = colors.textTitle,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 메시지 리스트
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(
                        message = message,
                        colors = colors,
                        onInsertToMemo = if (message.role == "assistant") {
                            { onInsertToMemo(message.content) }
                        } else null
                    )
                }

                // 스트리밍 중인 응답
                if (streamingText != null) {
                    item {
                        MessageBubble(
                            message = AiAssistMessage(role = "assistant", content = streamingText),
                            colors = colors,
                            onInsertToMemo = null // 스트리밍 중에는 삽입 불가
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 입력 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.chipBackground.copy(alpha = 0.3f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(
                        color = colors.textBody,
                        fontSize = fontSettings.scaled(14)
                    ),
                    cursorBrush = SolidColor(colors.textTitle),
                    singleLine = false,
                    maxLines = 3,
                    decorationBox = { innerTextField ->
                        Box {
                            if (inputText.isEmpty()) {
                                Text(
                                    text = "무엇이든 물어보세요",
                                    color = colors.textBody.copy(alpha = 0.4f),
                                    fontSize = fontSettings.scaled(14)
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 전송 버튼
                val canSend = inputText.isNotBlank() && !isLoading
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (canSend) colors.chipText
                            else colors.chipBackground.copy(alpha = 0.3f)
                        )
                        .clickable(enabled = canSend) {
                            val text = inputText.trim()
                            inputText = ""
                            onSendMessage(text)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        tint = if (canSend) colors.cardBackground else colors.textBody.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: AiAssistMessage,
    colors: AppColors,
    onInsertToMemo: (() -> Unit)?
) {
    val fontSettings = LocalFontSettings.current
    val isUser = message.role == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isUser) colors.chipText.copy(alpha = 0.15f)
                    else colors.chipBackground.copy(alpha = 0.3f)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .then(
                    if (isUser) Modifier.fillMaxWidth(0.85f)
                    else Modifier.fillMaxWidth(0.95f)
                )
        ) {
            Text(
                text = message.content,
                fontSize = fontSettings.scaled(14),
                color = colors.textBody,
                lineHeight = fontSettings.scaled(20)
            )
        }

        // "메모에 추가" 버튼 (assistant 메시지만, 스트리밍 완료 후)
        if (onInsertToMemo != null) {
            Row(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onInsertToMemo() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = colors.textBody.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "메모에 추가",
                    fontSize = fontSettings.scaled(12),
                    color = colors.textBody.copy(alpha = 0.6f)
                )
            }
        }
    }
}
