package com.adong.adchat.ui.markdown

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NovelReadingTest {
    @Test
    fun chapterLineBecomesHeadingButLongProseDoesNot() {
        assertTrue(isChapterHeading("第一章 雨夜"))
        assertTrue(isChapterHeading("Chapter 3"))
        assertFalse(isChapterHeading("第一章" + "雨".repeat(40)))
        assertFalse(isChapterHeading("他走进第一章提到的那条巷子。"))
    }

    @Test
    fun starredActionIsNotAListAndLongStarDoesNotEmphasize() {
        assertTrue(isStarredProseLine("* 他低声说*"))
        assertFalse(isStarredProseLine("* 一条普通列表"))
        assertTrue(emphasisSpanAllowed("微微一笑"))
        assertFalse(emphasisSpanAllowed("终于开口，把后面整段都斜体"))
    }

    @Test
    fun nameMarksHideDelimitersAndLeaveUnclosedTextAlone() {
        val spans = readingSpans("门外是/..沈青../，还有/..未写完")
        assertEquals(
            listOf(
                ReadingSpan.Text("门外是"),
                ReadingSpan.Name("沈青"),
                ReadingSpan.Text("，还有"),
                ReadingSpan.Text("/..未写完")
            ),
            spans
        )
    }

    @Test
    fun dialogueAndPlainEnglishAreNotIndented() {
        assertTrue(novelBodyDisplay("他推开门。").startsWith("　　"))
        assertEquals("「进来。」", novelBodyDisplay("「进来。」"))
        assertEquals("He opened the door.", novelBodyDisplay("He opened the door."))
    }
}
