package com.adong.adchat.ui

import com.adong.adchat.data.ChatMessage
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * 重试只对最后一条消息开放：retryMessage 会把失败的这一问一答追加到列表末尾，
 * 作用在中间消息上会把该问答挪到别的提问之后，对话顺序和发给模型的历史一起错乱。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ChatRetryScopeTest {
    private fun viewModel() = MainViewModel(RuntimeEnvironment.getApplication())

    private fun turn(prefix: String) = listOf(
        ChatMessage(id = ("$prefix-u").hashCode().toLong(), role = "user", content = "$prefix 的提问"),
        ChatMessage(id = ("$prefix-a").hashCode().toLong(), role = "assistant", content = "$prefix 的回答")
    )

    @Test
    fun retryingAMiddleErrorLeavesTheConversationUntouched() {
        val vm = viewModel()
        val failed = ChatMessage(id = 4242L, role = "assistant", content = "请求失败", isError = true)
        // [u1, a1(错误), u2, a2]：中间那条失败的重试必须被拒绝，任何改动都会让顺序错乱。
        val before = turn("1") + failed + turn("2")
        vm.messages.addAll(before)

        vm.retryMessage(failed.id)

        assertEquals(before.map { it.id }, vm.messages.map { it.id })
        assertEquals(before.map { it.content }, vm.messages.map { it.content })
    }

    @Test
    fun retryingAnUnknownMessageIsIgnored() {
        val vm = viewModel()
        vm.messages.addAll(turn("1"))
        vm.retryMessage(987654321L)
        assertEquals(2, vm.messages.size)
    }

    @Test
    fun continuingAMiddleInterruptedMessageIsIgnored() {
        val vm = viewModel()
        val interrupted = ChatMessage(id = 5150L, role = "assistant", content = "半截回答", isInterrupted = true)
        val before = turn("1") + interrupted + turn("2")
        vm.messages.addAll(before)

        vm.retryMessage(interrupted.id)

        assertEquals(before.map { it.id }, vm.messages.map { it.id })
    }
}
