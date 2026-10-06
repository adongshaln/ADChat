package com.adong.adchat.data

import org.junit.Assert.*
import org.junit.Test
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.util.concurrent.TimeUnit

class ChatApiPolicyTest {
    @Test fun gptModelsAlwaysUseResponses() {
        for (model in listOf("gpt-5.6-sol", "gpt-6-astra", "GPT-4.1", "openai/gpt-5", "gpt5")) {
            assertTrue(model, ApiProfile(chatModel = model).usesResponses())
        }
    }

    @Test fun grokModelsAlwaysUseResponses() {
        for (model in listOf("grok-4.6", "grok-4-fast", "Grok-3-mini", "xai/grok-2-1212")) {
            assertTrue(model, ApiProfile(chatModel = model).usesResponses())
        }
    }

    @Test fun otherModelsUseChatCompletions() {
        for (model in listOf("claude-test", "gemini-test", "not-gpt-model", "deepseek-r1")) {
            assertFalse(model, ApiProfile(chatModel = model).usesResponses())
        }
        assertFalse("not-gpt-model".isGptModel())
        assertFalse("not-gpt-model".isGrokModel())
    }

    @Test fun actualRequestModelOverridesProfileDefault() {
        assertTrue(ApiProfile(chatModel = "claude-test").usesResponses("openai/gpt-6-astra"))
        assertTrue(ApiProfile(chatModel = "gpt-5.6-sol").usesResponses("grok-4.6"))
        assertFalse(ApiProfile(chatModel = "gpt-5.6-sol").usesResponses("gemini-test"))
    }

    @Test fun adaptiveCacheNeverRoutesGptThroughChat() = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(MockResponse().setHeader("Content-Type", "text/event-stream").setBody(
                "data: {\"type\":\"response.output_text.delta\",\"delta\":\"OK\"}\n\n" +
                    "data: {\"type\":\"response.completed\",\"response\":{\"usage\":{}}}\n\n"
            ))
            val profile = ApiProfile(baseUrl = server.url("/").toString(), apiKey = "test",
                promptCacheMode = "adaptive", responsesPath = "/custom/responses")
            val result = ApiRepository().streamChat(profile, "gpt-5.6-sol", "",
                listOf(ChatMessage(role = "user", content = "hello")), "test") {}
            assertEquals("OK", result.text)
            assertEquals("/custom/responses", server.takeRequest(2, TimeUnit.SECONDS)?.path)
            assertEquals(1, server.requestCount)
        } finally { server.shutdown() }
    }
}
