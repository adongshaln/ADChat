package com.adong.adchat.ui.markdown

internal sealed class InlinePiece {
    data class Prose(val text: String) : InlinePiece()
    data class RemoteImage(val url: String, val alt: String) : InlinePiece()
}

private val MARKDOWN_IMAGE = Regex("""!\[([^\]\n]*)]\((https?://[^)\s]+)\)""")
private val WRAPPED_IMAGE = Regex("""\*\+-+(https?://[^\s*]+)\*\+-+""")
private val STAR_PLUS_IMAGE = Regex("""\*\+\s*(https?://\S+)""")
private val BARE_IMAGE = Regex(
    """https?://[^\s<>'"()\]]+\.(?:jpe?g|png|webp|gif|avif)(?:\?[^\s<>'"()\]]*)?""",
    RegexOption.IGNORE_CASE
)

private data class ImageHit(val start: Int, val end: Int, val url: String, val alt: String)

/** Pulls real image addresses out of prose so they can be shown instead of left as raw markup. */
internal fun splitInlineImages(text: String): List<InlinePiece> {
    val hits = mutableListOf<ImageHit>()
    MARKDOWN_IMAGE.findAll(text).forEach { match ->
        hits += ImageHit(match.range.first, match.range.last + 1, match.groupValues[2], match.groupValues[1])
    }
    WRAPPED_IMAGE.findAll(text).forEach { match ->
        hits += ImageHit(match.range.first, match.range.last + 1, match.groupValues[1], "")
    }
    STAR_PLUS_IMAGE.findAll(text).forEach { match ->
        val url = match.groupValues[1].trimEnd('*', '+', '-', ')', ']', ',', '。')
        if (url.isImageUrl()) hits += ImageHit(match.range.first, match.range.first + match.value.indexOf(url) + url.length, url, "")
    }
    BARE_IMAGE.findAll(text).forEach { match ->
        hits += ImageHit(match.range.first, match.range.last + 1, match.value, "")
    }
    val ordered = hits.sortedWith(compareBy<ImageHit> { it.start }.thenByDescending { it.end })
    val taken = mutableListOf<ImageHit>()
    for (hit in ordered) {
        if (taken.any { hit.start < it.end && hit.end > it.start }) continue
        taken += hit
    }
    taken.sortBy { it.start }
    if (taken.isEmpty()) return listOf(InlinePiece.Prose(text))
    val pieces = mutableListOf<InlinePiece>()
    var cursor = 0
    taken.forEach { hit ->
        if (hit.start > cursor) pieces += InlinePiece.Prose(text.substring(cursor, hit.start))
        pieces += InlinePiece.RemoteImage(hit.url, hit.alt)
        cursor = hit.end
    }
    if (cursor < text.length) pieces += InlinePiece.Prose(text.substring(cursor))
    return pieces.filterNot { it is InlinePiece.Prose && it.text.isBlank() }
}

private fun String.isImageUrl(): Boolean = BARE_IMAGE.matches(trim())
