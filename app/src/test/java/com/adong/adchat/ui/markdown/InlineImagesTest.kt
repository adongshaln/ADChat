package com.adong.adchat.ui.markdown

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InlineImagesTest {
    @Test
    fun markdownImageAndBareLinkBecomeImages() {
        val pieces = splitInlineImages("封面如下\n![](https://example.com/a.jpg)\n直链 https://cdn.example.com/b.png")
        assertEquals(InlinePiece.Prose("封面如下\n"), pieces[0])
        assertEquals(InlinePiece.RemoteImage("https://example.com/a.jpg", ""), pieces[1])
        assertTrue(pieces[2] is InlinePiece.Prose)
        assertEquals(InlinePiece.RemoteImage("https://cdn.example.com/b.png", ""), pieces[3])
    }

    @Test
    fun starPlusImageLinkIsNotLeftAsText() {
        val pieces = splitInlineImages("封面 *+https://img.example.com/cover.webp")
        assertEquals(InlinePiece.RemoteImage("https://img.example.com/cover.webp", ""), pieces[1])
    }
}
