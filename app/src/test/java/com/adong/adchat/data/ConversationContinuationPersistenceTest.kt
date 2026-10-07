package com.adong.adchat.data

import android.content.Context
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * continuation 标记要能在重启后活下来：丢了的话，应用代发的「继续生成」指令
 * 会在下次打开时长回一条用户气泡。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ConversationContinuationPersistenceTest {
    private fun store() = ConversationStore(RuntimeEnvironment.getApplication())

    @Test
    fun continuationFlagSurvivesSaveAndLoad() {
        val context = RuntimeEnvironment.getApplication<Context>()
        context.getSharedPreferences("adchat_conversations", Context.MODE_PRIVATE).edit().clear().commit()
        val conversation = Conversation(
            id = "conv-note",
            title = "带继续指令的对话",
            messages = listOf(
                ChatMessage(id = 1L, role = "user", content = "继续写"),
                ChatMessage(
                    id = 2L,
                    role = "user",
                    content = "请从上一条回复中断的位置继续，直接续写，不要重复已生成的内容。",
                    isContinuation = true
                ),
                ChatMessage(id = 3L, role = "assistant", content = "好的，接着上文……")
            )
        )

        store().save(listOf(conversation))
        val restored = store().load().first { it.id == "conv-note" }

        assertTrue(restored.messages[1].isContinuation)
        assertFalse(restored.messages[0].isContinuation)
        assertFalse(restored.messages[2].isContinuation)
    }
}
