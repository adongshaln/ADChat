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
    fun markdownAndBareAddressesAreLinksWithoutSwallowingPunctuation() {
        val linked = linkAt("见 [维基](https://example.com/a) 这里", 2)
        assertEquals("维基", linked?.label)
        assertEquals("https://example.com/a", linked?.url)
        val bare = linkAt("打开 https://example.com/path。", 3)
        assertEquals("https://example.com/path", bare?.url)
        assertTrue(bare!!.end < "打开 https://example.com/path。".length)
    }
}
