package com.adong.adchat.data.story

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoryCastTest {
    @Test
    fun characterCardIgnoresSurroundingProse() {
        val card = StoryCharacterCard.parse("好的。\n```json\n{\"name\":\"林夏\",\"description\":\"住在旧书店\",\"personality\":\"寡言\",\"scenario\":\"雨夜\"}\n```")
        assertEquals("林夏", card.name)
        assertTrue(card.description.contains("旧书店"))
    }

    @Test
    fun personaIsJustTheUser() {
        assertEquals("旁白，偶尔下场", parseUserPersona("{\"persona\":\"旁白，偶尔下场\"}"))
    }

    @Test
    fun confirmedCastIsSentWithTheStoryPrompt() {
        val result = StoryContextComposer.compose(
            workspace = StoryWorkspace.Prose,
            baseInstruction = "写正文",
            memoryRecords = emptyList(),
            proposals = emptyList(),
            proseMessages = emptyList(),
            discussionMessages = emptyList(),
            cast = StoryCastContext(
                card = StoryCharacterCard("梅尔文", "一个人", "冷淡", "Fate/strange Fake 同人"),
                userPersona = "旁白"
            )
        )
        assertTrue(result.systemPrompt.contains("梅尔文"))
        assertTrue(result.systemPrompt.contains("Fate/strange Fake 同人"))
        assertTrue(result.systemPrompt.contains("旁白"))
        assertFalse(result.systemPrompt.contains("{{"))
    }
}
