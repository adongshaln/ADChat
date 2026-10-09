package com.adong.adchat.ui.markdown

internal const val NAME_MARK_OPEN = "/.."
internal const val NAME_MARK_CLOSE = "../"

private val CHAPTER_HEADING = Regex(
    """^(第\s*[0-9０-９一二三四五六七八九十百千零两〇]+\s*[章节回卷部篇幕]|序章|楔子|尾声|终章|番外|后记|前言|Chapter\s+\d+).*$""",
    RegexOption.IGNORE_CASE
)

internal fun isChapterHeading(line: String): Boolean {
    val text = line.trim()
    if (text.isEmpty() || text.length > 40) return false
    return CHAPTER_HEADING.matches(text)
}

internal fun isSceneBreak(line: String): Boolean {
    val value = line.filterNot(Char::isWhitespace)
    if (value in setOf("***", "——", "……", "...", "◇", "◆", "※")) return true
    if (value.length >= 3 && value.all { it == '-' || it == '*' || it == '＊' || it == '※' || it == '·' || it == '•' }) return true
    return false
}

/** `* 他低声说*` is emphasis, not a Markdown bullet. */
internal fun isStarredProseLine(line: String): Boolean {
    val trimmed = line.trim()
    return trimmed.length > 2 && trimmed.startsWith("*") && trimmed.endsWith("*") && !trimmed.startsWith("**")
}

/** Single-marker emphasis must stay short, or one stray star italicizes the rest of a paragraph. */
internal fun emphasisSpanAllowed(inner: String): Boolean {
    if (inner.isEmpty() || inner.length > 32) return false
    if (inner.any { it == '\n' || it == '。' || it == '！' || it == '？' || it == '!' || it == '?' || it == '，' }) return false
    if (inner.all { it.isWhitespace() || it == '*' || it == '_' || it == '·' || it == '•' }) return false
    return true
}

internal sealed class ReadingSpan {
    data class Text(val value: String) : ReadingSpan()
    data class Name(val value: String) : ReadingSpan()
}

/** Hides `/..` `../` and keeps the name as its own span. An unclosed mark stays literal. */
internal fun readingSpans(text: String): List<ReadingSpan> {
    if (!text.contains(NAME_MARK_OPEN)) return listOf(ReadingSpan.Text(text))
    val result = mutableListOf<ReadingSpan>()
    var index = 0
    while (index < text.length) {
        val start = text.indexOf(NAME_MARK_OPEN, index)
        if (start < 0) {
            result += ReadingSpan.Text(text.substring(index))
            break
        }
        if (start > index) result += ReadingSpan.Text(text.substring(index, start))
        val end = text.indexOf(NAME_MARK_CLOSE, start + NAME_MARK_OPEN.length)
        if (end < 0) {
            result += ReadingSpan.Text(text.substring(start))
            break
        }
        val name = text.substring(start + NAME_MARK_OPEN.length, end)
        if (name.isBlank()) result += ReadingSpan.Text(text.substring(start, end + NAME_MARK_CLOSE.length))
        else result += ReadingSpan.Name(name)
        index = end + NAME_MARK_CLOSE.length
    }
    return result
}
