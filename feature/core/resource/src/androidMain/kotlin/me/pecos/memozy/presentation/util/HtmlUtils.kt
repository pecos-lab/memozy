package me.pecos.memozy.presentation.util

private val HTML5_ENTITIES = mapOf(
    "excl" to "!", "quot" to "\"", "num" to "#", "dollar" to "$",
    "percnt" to "%", "amp" to "&", "apos" to "'", "lpar" to "(",
    "rpar" to ")", "ast" to "*", "plus" to "+", "comma" to ",",
    "period" to ".", "sol" to "/", "colon" to ":", "semi" to ";",
    "lt" to "<", "equals" to "=", "gt" to ">", "quest" to "?",
    "commat" to "@", "lsqb" to "[", "bsol" to "\\", "rsqb" to "]",
    "Hat" to "^", "lowbar" to "_", "grave" to "`", "lbrace" to "{",
    "vert" to "|", "rbrace" to "}", "tilde" to "~",
    "nbsp" to " ", "iexcl" to "\u00A1", "cent" to "\u00A2",
    "pound" to "\u00A3", "yen" to "\u00A5", "copy" to "\u00A9",
    "reg" to "\u00AE", "deg" to "\u00B0", "micro" to "\u00B5",
    "middot" to "\u00B7", "laquo" to "\u00AB", "raquo" to "\u00BB",
    "ndash" to "\u2013", "mdash" to "\u2014",
    "lsquo" to "\u2018", "rsquo" to "\u2019",
    "ldquo" to "\u201C", "rdquo" to "\u201D",
    "bull" to "\u2022", "hellip" to "\u2026", "trade" to "\u2122",
)

private val ENTITY_REGEX = Regex("&(\\w+);")

fun String.htmlToPlainText(): String =
    this
        .replace(Regex("<br\\s*/?>"), "\n")
        .replace(Regex("<[^>]+>"), "")
        .replace(ENTITY_REGEX) { match ->
            HTML5_ENTITIES[match.groupValues[1]] ?: match.value
        }
        .trim()

// HTML 구조에 필요한 엔티티는 유지하고 나머지만 디코딩
private val PRESERVE_ENTITIES = setOf("amp", "lt", "gt", "quot")

fun String.decodeHtmlEntities(): String =
    replace(ENTITY_REGEX) { match ->
        val name = match.groupValues[1]
        if (name in PRESERVE_ENTITIES) match.value
        else HTML5_ENTITIES[name] ?: match.value
    }
