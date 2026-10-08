package com.adong.adchat.ui.theme

import com.adong.adchat.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.robolectric.RuntimeEnvironment

class NotoSerifScFontResourceTest {
    @Test
    fun resourceIsAVariableSfntFontWithVariationTables() {
        val bytes = RuntimeEnvironment.getApplication().resources
            .openRawResource(R.font.noto_serif_sc_variable)
            .use { it.readBytes() }

        assertTrue(bytes.size > 20 * 1024 * 1024)
        assertEquals(0x00010000, u32(bytes, 0))

        val tableCount = u16(bytes, 4)
        val tags = (0 until tableCount).map { index ->
            val offset = 12 + index * 16
            String(bytes, offset, 4, Charsets.US_ASCII)
        }
        assertTrue(tags.contains("fvar"))
        assertTrue(tags.contains("gvar"))
    }

    private fun u16(bytes: ByteArray, offset: Int): Int =
        ((bytes[offset].toInt() and 0xff) shl 8) or
            (bytes[offset + 1].toInt() and 0xff)

    private fun u32(bytes: ByteArray, offset: Int): Int =
        ((bytes[offset].toInt() and 0xff) shl 24) or
            ((bytes[offset + 1].toInt() and 0xff) shl 16) or
            ((bytes[offset + 2].toInt() and 0xff) shl 8) or
            (bytes[offset + 3].toInt() and 0xff)
}
