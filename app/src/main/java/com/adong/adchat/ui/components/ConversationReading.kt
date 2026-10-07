package com.adong.adchat.ui.components

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.withFrameNanos

/** Reveal explicitly opened details above the floating composer, without jumping to the latest reply. */
suspend fun LazyListState.revealConversationDetails(index: Int, bottomClearancePx: Float) {
    stopScroll()
    withFrameNanos { }
    withFrameNanos { }
    val layout = layoutInfo
    val item = layout.visibleItemsInfo.firstOrNull { it.index == index }
    if (item != null) {
        val overflow = item.offset + item.size + bottomClearancePx - layout.viewportEndOffset
        if (overflow > 0f) animateScrollBy(overflow)
    } else {
        animateScrollToItem(index)
    }
}

/**
 * 用户是否已经停在末尾附近。聚焦输入框、键盘弹起这类自动跟随只在「本来就在看末尾」
 * 时才该发生；回看历史的人一碰输入框就被拽回底部，阅读位置就丢了。
 * 判据用「最后一个可见条目之后只剩一点余量」，而不是严格等于最后一条，
 * 免得列表末尾的留白让这个判断永远不成立。
 */
fun LazyListState.isCloseToBottom(toleranceItems: Int = 1): Boolean {
    val layout = layoutInfo
    val lastVisible = layout.visibleItemsInfo.maxByOrNull { it.index }?.index ?: return true
    if (lastVisible >= totalItemsCount - 1) return true
    val remaining = layout.viewportEndOffset -
        (layout.visibleItemsInfo.lastOrNull { it.index == lastVisible }?.offset ?: 0) -
        (layout.visibleItemsInfo.lastOrNull { it.index == lastVisible }?.size ?: 0)
    return remaining < layout.viewportSize * 0.12f
}
