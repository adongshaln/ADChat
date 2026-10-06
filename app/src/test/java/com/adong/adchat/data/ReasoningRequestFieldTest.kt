package com.adong.adchat.data

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.util.concurrent.TimeUnit

/**
 * 逐家族核对真实请求体：字段名和取值各家不通用，发错就是 400，页面上也看不出原因。
 */
class ReasoningRequestFieldTest {
    @Test fun gptWritesReasoningObjectOnResponsesApi() = runBlocking {
        val body = request("gpt-5.6-sol", "high", ModelContextLimits(131072, 8192))
        assertEquals("high", body.getJSONObject("reasoning").getString("effort"))
        assertFalse(body.has("reasoning_effort"))
        assertEquals(8192, body.getInt("max_output_tokens"))
    }

    @Test fun grokWritesTopLevelEffortAndKeepsSamplingControlsOut() = runBlocking {
        val body = request("grok-4.6", "xhigh", ModelContextLimits(131072, 8192))
        assertEquals("xhigh", body.getString("reasoning_effort"))
        assertFalse(body.has("reasoning"))
        assertFalse(body.has("presence_penalty"))
        assertFalse(body.has("frequency_penalty"))
        assertFalse(body.has("seed"))
    }

    @Test fun grokSkipsThinkingFieldsEntirelyOnDefault() = runBlocking {
        val body = request("grok-4.6", ReasoningPolicy.DEFAULT, ModelContextLimits(131072, 8192))
        assertFalse(body.has("reasoning_effort"))
        assertFalse(body.has("thinking"))
    }

    @Test fun claudeBudgetStaysBelowTheFinalMaxTokens() = runBlocking {
        val body = request("claude-sonnet-4.5", "high", ModelContextLimits(131072, 8192))
        val thinking = body.getJSONObject("thinking")
        assertEquals("enabled", thinking.getString("type"))
        assertTrue(thinking.getInt("budget_tokens") < body.getInt("max_tokens"))
        assertEquals(body.getInt("max_tokens") - 1024, thinking.getInt("budget_tokens"))
    }

    @Test fun claudeBudgetShrinksWhenAPresetLowersMaxTokens() = runBlocking {
        val body = request(
            model = "claude-sonnet-4.5", effort = "high",
            limits = ModelContextLimits(131072, 8192),
            generationOptions = ChatGenerationOptions(maxOutputTokens = 3072)
        )
        val thinking = body.getJSONObject("thinking")
        assertEquals(3072 - 1024, thinking.getInt("budget_tokens"))
        assertEquals(3072, body.getInt("max_tokens"))
    }

    @Test fun glmPairsThinkingSwitchWithEffort() = runBlocking {
        val body = request("glm-4.6", "low", ModelContextLimits(131072, 8192))
        assertEquals("enabled", body.getJSONObject("thinking").getString("type"))
        assertEquals("low", body.getString("reasoning_effort"))

        val off = request("glm-4.6", "none", ModelContextLimits(131072, 8192))
        assertEquals("disabled", off.getJSONObject("thinking").getString("type"))
        assertFalse(off.has("reasoning_effort"))
    }

    @Test fun kimiToggleModelsOnlyGetThinkingSwitch() = runBlocking {
        val on = request("kimi-k2.6", "on", ModelContextLimits(131072, 8192))
        assertEquals("enabled", on.getJSONObject("thinking").getString("type"))
        assertFalse(on.has("reasoning_effort"))

        val off = request("kimi-k2.6", "off", ModelContextLimits(131072, 8192))
        assertEquals("disabled", off.getJSONObject("thinking").getString("type"))
    }

    @Test fun kimiK3UsesEffortField() = runBlocking {
        val body = request("kimi-k3", "max", ModelContextLimits(131072, 8192))
        assertEquals("max", body.getString("reasoning_effort"))
        assertFalse(body.has("thinking"))
    }

    @Test fun deepSeekCombinesBothFields() = runBlocking {
        val body = request("deepseek-chat", "high", ModelContextLimits(131072, 8192))
        assertEquals("enabled", body.getJSONObject("thinking").getString("type"))
        assertEquals("high", body.getString("reasoning_effort"))
    }

    @Test fun unsupportedModelsReceiveNoThinkingFields() = runBlocking {
        val body = request("gemini-2.5-pro", "high", ModelContextLimits(131072, 8192))
        assertFalse(body.has("reasoning_effort"))
        assertFalse(body.has("reasoning"))
        assertFalse(body.has("thinking"))
        // 非推理模型照旧携带采样参数。
        assertEquals(0.4, body.getDouble("presence_penalty"), 0.0001)
        assertEquals(11, body.getInt("seed"))
    }

    @Test fun defaultEffortWritesNothingAnywhere() = runBlocking {
        val body = request("claude-sonnet-4.5", ReasoningPolicy.DEFAULT, null, ChatGenerationOptions(presencePenalty = 0.4, seed = 11))
        assertFalse(body.has("reasoning_effort"))
        assertFalse(body.has("reasoning"))
        assertFalse(body.has("thinking"))
        assertFalse(body.has("max_tokens"))
    }

    /** 走一遍真实请求，把 MockWebServer 收到的报文解析出来。 */
    private suspend fun request(
        model: String,
        effort: String,
        limits: ModelContextLimits?,
        generationOptions: ChatGenerationOptions = ChatGenerationOptions(presencePenalty = 0.4, seed = 11)
    ): JSONObject {
        val server = MockWebServer()
        server.start()
        try {
            // MockWebServer 只按顺序回包，不认路径；先确定协议，只排对应格式的那一份。
            val responsesApi = ApiProfile(chatModel = model).usesResponses()
            server.enqueue(
                if (responsesApi) sse(
                    "data: {\"type\":\"response.output_text.delta\",\"delta\":\"好\"}\n\n" +
                        "data: {\"type\":\"response.completed\",\"response\":{\"usage\":{}}}\n\n"
                ) else sse(
                    "data: {\"choices\":[{\"delta\":{\"content\":\"好\"}}]}\n\n" +
                        "data: {\"choices\":[{\"delta\":{},\"finish_reason\":\"stop\"}]}\n\n" +
                        "data: [DONE]\n\n"
                )
            )
            val profile = ApiProfile(
                baseUrl = server.url("/").toString(),
                apiKey = "test",
                chatModel = model,
                reasoningEffort = effort,
                modelContexts = limits?.let { mapOf(model to it) } ?: emptyMap()
            )
            val result = ApiRepository().streamChat(
                profile, model, "", listOf(ChatMessage(role = "user", content = "hi")), "test",
                generationOptions = generationOptions
            ) {}
            assertEquals("好", result.text)
            return JSONObject(recordedBody(server, responsesApi))
        } finally { server.shutdown() }
    }

    private fun recordedBody(server: MockWebServer, responsesApi: Boolean): String {
        val request = server.takeRequest(2, TimeUnit.SECONDS) ?: return ""
        check(request.path == (if (responsesApi) "/v1/responses" else "/v1/chat/completions"))
        return request.body.readUtf8()
    }

    private fun sse(body: String): MockResponse = MockResponse()
        .setHeader("Content-Type", "text/event-stream")
        .setBody(body)
}
