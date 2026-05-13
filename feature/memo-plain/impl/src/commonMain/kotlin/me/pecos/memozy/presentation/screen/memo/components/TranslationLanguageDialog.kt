package me.pecos.memozy.presentation.screen.memo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.pecos.memozy.feature.core.resource.generated.resources.Res
import me.pecos.memozy.feature.core.resource.generated.resources.cancel
import me.pecos.memozy.feature.core.resource.generated.resources.confirm
import me.pecos.memozy.feature.core.resource.generated.resources.my_language
import me.pecos.memozy.feature.core.resource.generated.resources.realtime_translation
import me.pecos.memozy.feature.core.resource.generated.resources.select_language
import me.pecos.memozy.feature.core.resource.generated.resources.target_language
import me.pecos.memozy.feature.core.resource.generated.resources.translation_same_language_warning
import me.pecos.memozy.presentation.components.AppPopup
import me.pecos.memozy.presentation.components.PopupActionArea
import me.pecos.memozy.presentation.components.PopupNavigation
import me.pecos.memozy.presentation.components.PopupSize
import me.pecos.memozy.presentation.theme.LocalAppColors
import org.jetbrains.compose.resources.stringResource

private val SUPPORTED_LANGS = listOf(
    "ko" to "한국어",
    "en" to "English",
    "ja" to "日本語",
    "zh" to "中文",
)

private fun labelOf(code: String?): String =
    SUPPORTED_LANGS.firstOrNull { it.first == code }?.second ?: ""

/**
 * Wanted Popup 가이드 — AppPopup (Medium / Emphasized / Compact).
 * Dropdown 은 popup 과 동일 톤(흰색 + 옅은 border + rounded)으로 통일.
 */
@Composable
fun TranslationLanguageDialog(
    sourceLang: String?,
    targetLang: String?,
    onSourceLangSelected: (String) -> Unit,
    onTargetLangSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val colors = LocalAppColors.current
    val canConfirm = sourceLang != null && targetLang != null && sourceLang != targetLang
    val placeholderText = stringResource(Res.string.select_language)
    AppPopup(
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.realtime_translation),
        navigation = PopupNavigation.EMPHASIZED,
        size = PopupSize.MEDIUM,
        actionArea = PopupActionArea.COMPACT,
        primaryButtonText = stringResource(Res.string.confirm),
        onPrimaryClick = if (canConfirm) onConfirm else null,
        secondaryButtonText = stringResource(Res.string.cancel),
        onSecondaryClick = onDismiss,
    ) {
        Text(stringResource(Res.string.my_language), fontWeight = FontWeight.Medium, fontSize = 14.sp, color = colors.textBody)
        Spacer(modifier = Modifier.height(8.dp))
        LanguageDropdown(
            selectedLabel = labelOf(sourceLang),
            placeholder = placeholderText,
            onSelected = onSourceLangSelected,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(Res.string.target_language), fontWeight = FontWeight.Medium, fontSize = 14.sp, color = colors.textBody)
        Spacer(modifier = Modifier.height(8.dp))
        LanguageDropdown(
            selectedLabel = labelOf(targetLang),
            placeholder = placeholderText,
            onSelected = onTargetLangSelected,
        )
        if (sourceLang != null && targetLang != null && sourceLang == targetLang) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(Res.string.translation_same_language_warning),
                fontSize = 12.sp,
                color = Color(0xFFE24B4A),
            )
        }
    }
}

@Composable
private fun LanguageDropdown(
    selectedLabel: String,
    placeholder: String,
    onSelected: (String) -> Unit,
) {
    val colors = LocalAppColors.current
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .border(
                    width = 1.dp,
                    color = colors.cardBorder,
                    shape = RoundedCornerShape(10.dp),
                )
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = selectedLabel.ifBlank { placeholder },
                fontSize = 14.sp,
                color = if (selectedLabel.isBlank()) colors.textSecondary.copy(alpha = 0.7f) else colors.textBody,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White),
        ) {
            SUPPORTED_LANGS.forEach { (code, label) ->
                DropdownMenuItem(
                    text = { Text(label, color = colors.textBody) },
                    onClick = {
                        onSelected(code)
                        expanded = false
                    },
                )
            }
        }
    }
}
