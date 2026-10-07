package com.adong.adchat.ui

import com.adong.adchat.data.ChatImageAttachment
import com.adong.adchat.data.ChatMessage
import com.adong.adchat.data.Conversation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * 附件属于「还没发出去的那个草稿」，不属于输入框本身：切会话 / 新建会话必须清空，
 * 否则上个对话选的图会跟着新会话的草稿一起发出去，用户完全不知情。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ChatAttachmentScopeTest {
    private fun viewModel() = MainViewModel(RuntimeEnvironment.getApplication())

    private fun attachment(id: String) = ChatImageAttachment(
        id = id, uri = "content://media/$id.jpg", name = "$id.jpg"
    )

    private fun conversation(id: String, title: String) = Conversation(
        id = id,
        title = title,
        messages = listOf(ChatMessage(id = id.hashCode().toLong(), role = "user", content = "$title 的提问")),
        createdAt = 1L,
        updatedAt = 1L,
        profileId = "",
        model = ""
    )

    @Test
    fun switchingConversationDropsUnsentAttachments() {
        val vm = viewModel()
        vm.conversations.add(conversation("conversation-b", "对话 B"))
        vm.chatAttachments.addAll(listOf(attachment("a"), attachment("b")))

        vm.selectConversation("conversation-b")

        assertTrue(vm.chatAttachments.isEmpty())
    }

    @Test
    fun newConversationDropsUnsentAttachments() {
        val vm = viewModel()
        vm.chatAttachments.addAll(listOf(attachment("a"), attachment("b"), attachment("c")))

        vm.newConversation()

        assertTrue(vm.chatAttachments.isEmpty())
    }

    @Test
    fun switchingToAnUnknownConversationLeavesEverythingAlone() {
        val vm = viewModel()
        vm.chatAttachments.addAll(listOf(attachment("a")))

        vm.selectConversation("no-such-conversation")

        assertEquals(1, vm.chatAttachments.size)
    }
}
