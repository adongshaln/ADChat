package com.adong.adchat.data.story

import org.junit.Assert.assertEquals
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
}
