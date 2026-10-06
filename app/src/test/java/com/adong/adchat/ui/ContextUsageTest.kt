package com.adong.adchat.ui

import com.adong.adchat.data.ModelContextLimits
import org.junit.Assert.*
import org.junit.Test

/** 圆圈按钮画出来的百分比必须和面板里的数字一致，边界情况单独钉一下。 */
class ContextUsageTest {
    private fun usage(total: Int, limits: ModelContextLimits?): ContextUsage = ContextUsage(
        model = "gpt-5.6-sol",
        limits = limits,
        systemTokens = total,
        historyTokens = 0,
        attachmentTokens = 0,
        overheadTokens = 0,
        omittedTurns = 0,
        lastRequestInputTokens = 0,
        lastRequestOutputTokens = 0,
        lastRequestReasoningTokens = 0
    )

    @Test fun withoutConfiguredLimitsTheRingShowsUnknownNotZero() {
        val unknown = usage(4_096, limits = null)
        assertFalse(unknown.configured)
        assertEquals(0, unknown.budgetTokens)
        assertEquals(0.0, unknown.progress.toDouble(), 0.0)
        assertEquals(0, unknown.percent)
        assertFalse(unknown.overflow)
    }

    @Test fun percentMatchesTheBudgetTheSheetCalculates() {
        val limits = ModelContextLimits(131_072, 8_192)
        val budget = limits.inputTokens
        assertTrue(budget > 0)

        val usage = usage(10_000, limits)
        assertEquals((10_000 * 100) / budget, usage.percent)
        assertEquals(10_000.0 / budget, usage.progress.toDouble(), 0.001)
        assertFalse(usage.overflow)

        // 用不满 1% 时显示 0%，但面板里的明细仍按真实数值累加。
        val tiny = usage(1_000, limits)
        assertEquals(0, tiny.percent)
        assertEquals(1_000, tiny.totalTokens)
        assertFalse(tiny.overflow)
    }

    @Test fun overflowIsCappedAtFullCircleAndFlagged() {
        val limits = ModelContextLimits(131_072, 8_192)
        val way = usage(200_000, limits)
        assertEquals(1.0, way.progress.toDouble(), 0.0)
        assertEquals(100, way.percent)
        assertTrue(way.overflow)
    }

    @Test fun totalsAddUpEveryComponentTheSheetLists() {
        val usage = ContextUsage(
            model = "claude-sonnet-4.5",
            limits = ModelContextLimits(131_072, 8_192),
            systemTokens = 300,
            historyTokens = 2_400,
            attachmentTokens = 4_096,
            overheadTokens = 1_024,
            omittedTurns = 3,
            lastRequestInputTokens = 6_500,
            lastRequestOutputTokens = 700,
            lastRequestReasoningTokens = 250
        )
        assertEquals(300 + 2_400 + 4_096 + 1_024, usage.totalTokens)
        assertTrue(usage.configured)
    }
}
