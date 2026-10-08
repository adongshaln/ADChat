package com.adong.adchat.data

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptCacheTest {
    @Test fun familiesFollowTheModelNotTheGatewayPrefix() {
        assertEquals(PromptCacheFamily.Claude, "anthropic/claude-sonnet-4.5".promptCacheFamily())
        assertEquals(PromptCacheFamily.Grok, "xai/grok-4".promptCacheFamily())
        assertEquals(PromptCacheFamily.Gpt, "openai/gpt-5.4".promptCacheFamily())
        assertEquals(PromptCacheFamily.None, "deepseek-v3".promptCacheFamily())
    }

    @Test fun claudeBreakpointsCoverSystemPreviousTurnAndLatestMessage() {
        val messages = JSONArray()
            .put(JSONObject().put("role", "system").put("content", "你是 Aster"))
            .put(JSONObject().put("role", "user").put("content", "第一问"))
            .put(JSONObject().put("role", "assistant").put("content", "第一答"))
            .put(JSONObject().put("role", "user").put("content", "第二问"))
        PromptCache.markClaudeMessages(messages)

        assertEquals("1h", control(messages, 0).getString("ttl"))
        assertFalse(messages.getJSONObject(1).get("content") is JSONArray)
        assertEquals("ephemeral", control(messages, 2).getString("type"))
        assertEquals("1h", control(messages, 3).getString("ttl"))
    }

    @Test fun claudeImageMessageKeepsTheImageAndMarksTheLastBlock() {
        val image = JSONObject().put("type", "image_url")
        val messages = JSONArray().put(JSONObject().put("role", "user").put("content", JSONArray()
            .put(JSONObject().put("type", "text").put("text", "看图"))
            .put(image)))
        PromptCache.markClaudeMessages(messages)
        val content = messages.getJSONObject(0).getJSONArray("content")
        assertFalse(content.getJSONObject(0).has("cache_control"))
        assertEquals("ephemeral", content.getJSONObject(1).getJSONObject("cache_control").getString("type"))
    }

    @Test fun grokGetsAStickyKeyWithoutOpenAiCacheOptions() {
        val body = JSONObject()
        PromptCache.apply(body, enabled = true, model = "grok-4", cacheKey = "conversation-1", responsesApi = true)
        assertEquals("conversation-1", body.getString("prompt_cache_key"))
        assertFalse(body.has("prompt_cache_options"))
        assertEquals("grok-sticky", PromptCache.strategy(true, "grok-4", responsesApi = true))
    }

    @Test fun olderGptModelsAlsoGetTheCacheKey() {
        val body = JSONObject()
        PromptCache.apply(body, enabled = true, model = "gpt-4.1", cacheKey = "conversation-1", responsesApi = true)
        assertEquals("conversation-1", body.getString("prompt_cache_key"))
        assertEquals("automatic", PromptCache.strategy(true, "gpt-4.1", responsesApi = true))
    }

    @Test fun disabledCacheLeavesTheBodyAlone() {
        val body = JSONObject()
        PromptCache.apply(body, enabled = false, model = "claude-sonnet-4.5", cacheKey = "conversation-1", responsesApi = false)
        assertFalse(body.has("prompt_cache_key"))
        assertFalse(PromptCache.requested(false, "claude-sonnet-4.5"))
    }

    private fun control(messages: JSONArray, index: Int): JSONObject =
        messages.getJSONObject(index).getJSONArray("content").getJSONObject(0).getJSONObject("cache_control")
}
