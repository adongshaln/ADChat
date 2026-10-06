package com.adong.adchat.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adong.adchat.data.ContextWindowPresets
import com.adong.adchat.data.ModelContextLimits
import com.adong.adchat.ui.theme.*

/**
 * 输入框左侧的上下文进度圈。外圈实时反映本次请求预计占用的输入预算，
 * 中间是百分比；没有为该模型配置上下文长度时显示为未知状态。
 */
@Composable
fun ComposerContextRing(
    percent: Int,
    configured: Boolean,
    overflow: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = when {
        !configured -> MutedInk
        overflow -> Danger
        percent >= 85 -> Accent
        else -> Sage
    }
    val progress by animateFloatAsState(
        targetValue = if (configured) (percent / 100f).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(220),
        label = "context-ring"
    )
    IconButton(onClick = onClick, modifier = modifier.size(44.dp)) {
        Box(Modifier.size(30.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = tint,
                trackColor = Hairline.copy(alpha = .55f),
                strokeWidth = 3.dp
            )
            Text(
                if (configured) "$percent%" else "--",
                color = tint,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/** 输入框上的思考强度按钮：图标 + 当前档位短名，点开按模型匹配的选项。 */
@Composable
fun ComposerReasoningButton(
    label: String,
    detailed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = if (detailed) Accent else MutedInk
    IconButton(onClick = onClick, modifier = modifier.size(44.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.Psychology, null, Modifier.size(21.dp), tint = tint)
            Text(label, color = tint, fontSize = 9.sp, maxLines = 1)
        }
    }
}

/**
 * 上下文详情：本次请求预计占用、预算构成、模型实际用量，并可直接改上下文长度。
 * [onCustomize] 打开精确数值对话框，[onReset] 恢复默认（不设上限时的服务端行为）。
 */
@Composable
fun ComposerContextSheet(
    model: String,
    limits: ModelContextLimits?,
    totalTokens: Int,
    systemTokens: Int,
    historyTokens: Int,
    attachmentTokens: Int,
    overheadTokens: Int,
    omittedTurns: Int,
    lastRequestInputTokens: Int,
    lastRequestOutputTokens: Int,
    lastRequestReasoningTokens: Int,
    onSelectWindow: (Int) -> Unit,
    onCustomize: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val budget = limits?.inputTokens ?: 0
    val progress = if (budget > 0) (totalTokens.toFloat() / budget).coerceIn(0f, 1f) else 0f
    val overflow = budget > 0 && totalTokens > budget
    AsterOptionsSheet(
        title = "上下文",
        subtitle = model.ifBlank { "未选择模型" },
        onDismiss = onDismiss,
        headerIcon = Icons.Rounded.Tune
    ) {
        Surface(color = Surface, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (budget > 0) "$totalTokens / $budget Token" else "$totalTokens Token（未设预算）",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            if (overflow) "已超出输入预算，最早的历史会被整轮省略" else "本次请求预计占用",
                            color = if (overflow) Danger else MutedInk,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Text(
                        if (budget > 0) "${(progress * 100).toInt()}%" else "--",
                        color = if (overflow) Danger else Accent,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)),
                    color = if (overflow) Danger else Accent,
                    trackColor = Hairline.copy(alpha = .5f)
                )
                Text(
                    if (limits == null) {
                        "还没有为这个模型设置上下文长度，客户端不会做本地预算控制"
                    } else {
                        "窗口 ${ContextWindowPresets.label(limits.windowTokens)} · 输出上限 ${limits.outputTokens} · 安全余量 ${limits.safetyTokens}"
                    },
                    color = MutedInk,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            ContextBreakLine("系统提示词", systemTokens)
            ContextBreakLine("对话历史与工具记录", historyTokens)
            ContextBreakLine("图片附件", attachmentTokens, hint = "每张约 4K")
            ContextBreakLine("请求开销", overheadTokens, hint = "协议与工具定义")
            if (omittedTurns > 0) {
                Text(
                    "为塞进预算，已省略最早 $omittedTurns 轮对话；本地记录仍保留",
                    color = Danger,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        if (lastRequestInputTokens > 0 || lastRequestOutputTokens > 0) {
            Surface(color = Canvas, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = 13.dp, vertical = 11.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("上一次请求（供应商回传）", color = MutedInk, style = MaterialTheme.typography.labelMedium)
                    Text(
                        "输入 $lastRequestInputTokens · 输出 $lastRequestOutputTokens" +
                            if (lastRequestReasoningTokens > 0) "（含推理 $lastRequestReasoningTokens）" else "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Text("上下文长度", style = MaterialTheme.typography.titleSmall)
        Text(
            "按这个模型实际可用的窗口设置；只影响客户端预算与 max_tokens，不会改变服务端真实容量。",
            color = MutedInk,
            style = MaterialTheme.typography.labelMedium
        )
        ModelContextPresetsInline(
            limits = limits,
            onSelect = onSelectWindow
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onCustomize, enabled = model.isNotBlank(), modifier = Modifier.weight(1f)) {
                Text("自定义数值")
            }
            OutlinedButton(onClick = onReset, enabled = limits != null, modifier = Modifier.weight(1f)) {
                Text("恢复默认")
            }
        }
    }
}

/** 面板里直接铺开四档窗口，不用再点一层展开。 */
@Composable
private fun ModelContextPresetsInline(limits: ModelContextLimits?, onSelect: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ContextWindowPresets.values.forEach { (window, label) ->
            val selected = limits?.windowTokens == window
            Surface(
                onClick = { onSelect(window) },
                color = if (selected) AccentSoft else Canvas,
                contentColor = if (selected) Accent else Ink,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    1.dp,
                    if (selected) Accent.copy(alpha = .35f) else Hairline.copy(alpha = .6f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    if (selected) {
                        Text("当前", color = Accent, style = MaterialTheme.typography.labelMedium)
                    } else {
                        Text(
                            ContextWindowPresets.label(window),
                            color = MutedInk,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContextBreakLine(label: String, tokens: Int, hint: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, color = MutedInk, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(
            if (hint == null) "$tokens" else "$tokens · $hint",
            color = if (tokens > 0) Ink else MutedInk,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
