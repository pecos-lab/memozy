package me.pecos.memozy.presentation.screen.memo

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import org.json.JSONArray
import org.json.JSONObject

data class TextSpanStyle(
    val start: Int,
    val end: Int,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val strikethrough: Boolean = false,
    val color: String? = null
)

fun List<TextSpanStyle>.toJson(): String {
    val arr = JSONArray()
    for (s in this) {
        val obj = JSONObject()
        obj.put("s", s.start)
        obj.put("e", s.end)
        if (s.bold) obj.put("b", true)
        if (s.italic) obj.put("i", true)
        if (s.strikethrough) obj.put("st", true)
        if (s.color != null) obj.put("c", s.color)
        arr.put(obj)
    }
    return arr.toString()
}

fun String?.toTextSpanStyles(): List<TextSpanStyle> {
    if (this.isNullOrBlank()) return emptyList()
    return try {
        val arr = JSONArray(this)
        (0 until arr.length()).map { idx ->
            val obj = arr.getJSONObject(idx)
            TextSpanStyle(
                start = obj.getInt("s"),
                end = obj.getInt("e"),
                bold = obj.optBoolean("b", false),
                italic = obj.optBoolean("i", false),
                strikethrough = obj.optBoolean("st", false),
                color = obj.optString("c", null)
            )
        }
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

private fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        Color.Unspecified
    }
}
