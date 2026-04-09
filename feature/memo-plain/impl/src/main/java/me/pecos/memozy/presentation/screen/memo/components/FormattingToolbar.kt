package me.pecos.memozy.presentation.screen.memo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.model.RichTextState
import me.pecos.memozy.presentation.theme.AppColors

@Composable
fun FormattingToolbar(
    richTextState: RichTextState,
    colors: AppColors
) {
    var showColorPicker by remember { mutableStateOf(false) }
    var savedColorSelection by remember { mutableStateOf(TextRange.Zero) }

    val activeBg = colors.chipBackground
    val activeTint = colors.chipText

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isBold = richTextState.currentSpanStyle.fontWeight == FontWeight.Bold
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                .background(if (isBold) activeBg else activeBg.copy(alpha = 0.4f))
                .pointerInput(Unit) { detectTapGestures(onPress = { richTextState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)); tryAwaitRelease() }) },
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.FormatBold, contentDescription = null, tint = activeTint, modifier = Modifier.size(20.dp)) }

        val isItalic = richTextState.currentSpanStyle.fontStyle == FontStyle.Italic
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                .background(if (isItalic) activeBg else activeBg.copy(alpha = 0.4f))
                .pointerInput(Unit) { detectTapGestures(onPress = { richTextState.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)); tryAwaitRelease() }) },
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.FormatItalic, contentDescription = null, tint = activeTint, modifier = Modifier.size(20.dp)) }

        val isStrike = richTextState.currentSpanStyle.textDecoration == TextDecoration.LineThrough
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                .background(if (isStrike) activeBg else activeBg.copy(alpha = 0.4f))
                .pointerInput(Unit) { detectTapGestures(onPress = { richTextState.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)); tryAwaitRelease() }) },
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.FormatStrikethrough, contentDescription = null, tint = activeTint, modifier = Modifier.size(20.dp)) }

        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                .background(if (showColorPicker) activeBg else activeBg.copy(alpha = 0.4f))
                .pointerInput(Unit) { detectTapGestures(onPress = { savedColorSelection = richTextState.selection; showColorPicker = !showColorPicker; tryAwaitRelease() }) },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("A", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = activeTint)
                Box(modifier = Modifier.width(16.dp).height(3.dp).background(Color(0xFFFF0000), RoundedCornerShape(1.dp)))
            }
        }
    }

    if (showColorPicker) {
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("#000000", "#FF0000", "#FF9800", "#FFEB3B", "#4CAF50", "#2196F3", "#9C27B0", "#795548", "#607D8B").forEach { hex ->
                Box(
                    modifier = Modifier.size(22.dp).clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(hex)))
                        .pointerInput(hex) {
                            detectTapGestures(onPress = {
                                if (savedColorSelection.start != savedColorSelection.end) {
                                    richTextState.addSpanStyle(SpanStyle(color = Color(android.graphics.Color.parseColor(hex))), savedColorSelection)
                                }
                                showColorPicker = false; tryAwaitRelease()
                            })
                        }
                )
            }
        }
    }
}
