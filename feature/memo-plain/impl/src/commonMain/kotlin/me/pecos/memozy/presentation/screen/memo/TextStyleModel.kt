package me.pecos.memozy.presentation.screen.memo

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
data class TextSpanStyle(
    @SerialName("s") val start: Int,
    @SerialName("e") val end: Int,
    @SerialName("b") val bold: Boolean = false,
    @SerialName("i") val italic: Boolean = false,
    @SerialName("st") val strikethrough: Boolean = false,
    @SerialName("c") val color: String? = null
)

private val styleJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = false
}

private val styleListSerializer = ListSerializer(TextSpanStyle.serializer())

fun List<TextSpanStyle>.toJson(): String =
    styleJson.encodeToString(styleListSerializer, this)

fun String?.toTextSpanStyles(): List<TextSpanStyle> {
    if (this.isNullOrBlank()) return emptyList()
    return try {
        styleJson.decodeFromString(styleListSerializer, this)
    } catch (_: Exception) {
        emptyList()
    }
}

fun applyStylesToText(text: String, styles: List<TextSpanStyle>): AnnotatedString {
    return buildAnnotatedString {
        append(text)
        for (style in styles) {
            if (style.start >= text.length || style.end > text.length || style.start >= style.end) continue
            addStyle(
                SpanStyle(
                    fontWeight = if (style.bold) FontWeight.Bold else null,
                    fontStyle = if (style.italic) FontStyle.Italic else null,
                    textDecoration = if (style.strikethrough) TextDecoration.LineThrough else null,
                    color = style.color?.let { parseHexColor(it) } ?: Color.Unspecified
                ),
                start = style.start,
                end = style.end
            )
        }
    }
}

fun toggleStyle(
    styles: List<TextSpanStyle>,
    selectionStart: Int,
    selectionEnd: Int,
    bold: Boolean? = null,
    italic: Boolean? = null,
    strikethrough: Boolean? = null,
    color: String? = null
): List<TextSpanStyle> {
    if (selectionStart == selectionEnd) return styles

    val result = styles.toMutableList()
    val overlapping = result.filter { it.start < selectionEnd && it.end > selectionStart }

    val allBold = bold != null && overlapping.isNotEmpty() && overlapping.all { it.bold }
    val allItalic = italic != null && overlapping.isNotEmpty() && overlapping.all { it.italic }
    val allStrike = strikethrough != null && overlapping.isNotEmpty() && overlapping.all { it.strikethrough }

    result.removeAll(overlapping.toSet())

    for (existing in overlapping) {
        if (existing.start < selectionStart) {
            result.add(existing.copy(end = selectionStart))
        }
        if (existing.end > selectionEnd) {
            result.add(existing.copy(start = selectionEnd))
        }
    }

    val newStyle = TextSpanStyle(
        start = selectionStart,
        end = selectionEnd,
        bold = if (bold != null) !allBold else overlapping.firstOrNull()?.bold ?: false,
        italic = if (italic != null) !allItalic else overlapping.firstOrNull()?.italic ?: false,
        strikethrough = if (strikethrough != null) !allStrike else overlapping.firstOrNull()?.strikethrough ?: false,
        color = color ?: overlapping.firstOrNull()?.color
    )

    if (newStyle.bold || newStyle.italic || newStyle.strikethrough || newStyle.color != null) {
        result.add(newStyle)
    }

    return result.sortedBy { it.start }
}
