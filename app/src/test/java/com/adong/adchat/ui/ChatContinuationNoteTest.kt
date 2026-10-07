package com.adong.adchat.ui

import com.adong.adchat.data.ChatImageAttachment
import com.adong.adchat.data.ChatMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * 「继续生成」的指令由应用代发，用户从没打过这句话。它必须被标成 continuation，
 * 否则会在阅读流里长出一条用户气泡，还会污染标题推导和上下文估算。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ChatContinuationNoteTest {
    private fun viewModel() = MainViewModel(RuntimeEnvironment.getApplication())

    @Test
    fun continuingAnInterruptedReplySendsANoteNotAQuestion() {
        val vm = viewModel()
        val user = ChatMessage(id = 7300L, role = "user", content = "继续写下去")
        val interrupted = ChatMessage(id = 7301L, role = "assistant", content = "半截回答", isInterrupted = true)
        vm.messages.addAll(listOf(user, interrupted))

        vm.retryMessage(interrupted.id)

        // 发送是同步把消息挂上列表的，请求本身有没有发出去不影响这个断言。
        val note = vm.messages.firstOrNull { it.isContinuation }
        assertTrue("代发的续写指令必须带 continuation 标记", note != null)
        assertTrue(note!!.content.contains("从上一条回复中断的位置继续"))
        assertEquals(1, vm.messages.count { it.isContinuation })
    }

    @Test
    fun editingResendReloadsTheOriginalImagesAndText() {
        val vm = viewModel()
        vm.activeConversationId = "conv-edit"
        vm.messages.addAll(
            listOf(
                ChatMessage(
                    id = 7400L,
                    role = "user",
                    content = "这张图里有什么",
                    attachments = listOf(
                        ChatImageAttachment(id = "i1", uri = "content://media/1.jpg", name = "1.jpg"),
                        ChatImageAttachment(id = "i2", uri = "content://media/2.jpg", name = "2.jpg")
                    )
                ),
                ChatMessage(id = 7401L, role = "assistant", content = "一只猫")
            )
        )

        vm.editUserMessage(7400L)

        // 只搬正文会让重发后的模型看不到图，通常回答「我看不到图片」，而用户以为自己发过。
        assertEquals("这张图里有什么", vm.chatInput)
        assertEquals(2, vm.chatAttachments.size)
        assertTrue(vm.chatAttachments.map { it.uri }.contains("content://media/1.jpg"))
    }
}
