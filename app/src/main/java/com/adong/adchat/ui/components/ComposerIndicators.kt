package com.adong.adchat.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.adong.adchat.data.ContextWindowPresets
import com.adong.adchat.data.EffortStop
import com.adong.adchat.data.ModelContextLimits
import com.adong.adchat.data.ReasoningPolicy
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
    // 流式/打字时 percent 每个 token 都在变，220ms 的动画永远追不上目标，
    // 数字和圆弧长期不同步。输入先经 250ms 节流，只在值稳定下来后才平滑过渡。
    val settledPercent by produceState(initialValue = percent, percent) {
        kotlinx.coroutines.delay(250)
        value = percent
    }
    val progress by animateFloatAsState(
        targetValue = if (configured) (settledPercent / 100f).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(220),
        label = "context-ring"
    )
    IconButton(onClick = onClick, modifier = modifier.size(48.dp)) {
        Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(30.dp),
                color = tint,
                trackColor = Hairline.copy(alpha = .55f),
                strokeWidth = 3.dp
            )
            Text(
                if (configured) "$percent%" else "--",
                color = tint,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/** 输入框上的思考强度按钮。最高档会轻轻发光。 */
@Composable
fun ComposerReasoningButton(
    label: String,
    detailed: Boolean,
    glowing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pulse = if (glowing) maxGlowPulse() else 0f
    val tint = if (glowing) Accent else if (detailed) Accent else MutedInk
    val glow = Accent
    IconButton(onClick = onClick, modifier = modifier.size(48.dp)) {
        Box(contentAlignment = Alignment.Center) {
            if (glowing) {
                Canvas(Modifier.size(40.dp)) { drawMaxGlow(pulse, glow) }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.Psychology, null, Modifier.size(21.dp), tint = tint)
                Text(label, color = tint, fontSize = 11.sp, maxLines = 1)
            }
        }
    }
}

/** 三档滑杆。只有停在 Max 时拇指发光并带粒子。 */
@Composable
fun EffortSliderDialog(
    model: String,
    effort: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val stop = ReasoningPolicy.snapStop(model, effort)
    val recommended = ReasoningPolicy.recommendedStop(model)
    val haptic = LocalHapticFeedback.current
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            color = Surface,
            contentColor = Ink,
            shape = RoundedCornerShape(22.dp),
            shadowElevation = 8.dp,
            modifier = Modifier.width(300.dp).padding(horizontal = 24.dp)
        ) {
            Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                Text("思考  ${stop.label}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                EffortTrack(
                    stop = stop,
                    recommended = recommended,
                    onSelect = { next ->
                        if (next != stop) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelect(next.id)
                    }
                )
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("更快", color = MutedInk, style = MaterialTheme.typography.labelMedium)
                    Text("更聪明", color = MutedInk, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun EffortTrack(
    stop: EffortStop,
    recommended: EffortStop,
    onSelect: (EffortStop) -> Unit
) {
    val stops = EffortStop.entries
    val index by animateFloatAsState(stop.ordinal.toFloat(), tween(220), label = "effort-thumb")
    val glowing = stop == EffortStop.Max
    val pulse = if (glowing) maxGlowPulse() else 0f
    val drift = if (glowing) maxParticleDrift() else 0f
    val track = Hairline
    val fill = Accent
    val knob = if (glowing) Accent else Ink
    val plate = Surface
    Box(Modifier.fillMaxWidth().height(52.dp).pointerInput(stops) {
        detectHorizontalDragGestures { change, _ ->
            val pad = 14.dp.toPx()
            val span = (size.width - pad * 2f).coerceAtLeast(1f)
            val fraction = ((change.position.x - pad) / span).coerceIn(0f, 1f)
            val nearest = (fraction * stops.lastIndex).toInt().coerceIn(0, stops.lastIndex)
            val next = if (fraction * stops.lastIndex - nearest >= 0.5f) (nearest + 1).coerceAtMost(stops.lastIndex) else nearest
            onSelect(stops[next])
        }
    }) {
        Canvas(Modifier.fillMaxSize()) {
            val y = size.height / 2f
            val start = 14.dp.toPx()
            val end = size.width - 14.dp.toPx()
            val span = (end - start).coerceAtLeast(1f)
            val denom = stops.lastIndex.coerceAtLeast(1).toFloat()
            drawLine(track, Offset(start, y), Offset(end, y), strokeWidth = 3.dp.toPx())
            val thumbX = start + span * (index / denom)
            drawLine(fill, Offset(start, y), Offset(thumbX, y), strokeWidth = 3.dp.toPx())
            stops.forEach { item ->
                val x = start + span * item.ordinal / denom
                drawCircle(if (item.ordinal <= index) fill else track, radius = 3.5.dp.toPx(), center = Offset(x, y))
            }
            if (glowing) {
                drawMaxGlow(pulse, fill, Offset(thumbX, y))
                drawMaxParticles(drift, fill, Offset(thumbX, y))
            }
            drawCircle(plate, radius = 12.dp.toPx(), center = Offset(thumbX, y))
            drawCircle(knob, radius = 8.dp.toPx(), center = Offset(thumbX, y))
        }
        Row(Modifier.fillMaxSize()) {
            stops.forEach { item ->
                Box(Modifier.weight(1f).fillMaxHeight().clickable { onSelect(item) })
            }
        }
    }
    Row(Modifier.fillMaxWidth()) {
        stops.forEach { item ->
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                if (item == recommended) {
                    Text("推荐", color = MutedInk, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun maxGlowPulse(): Float {
    val transition = rememberInfiniteTransition(label = "effort-glow")
    val pulse by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "effort-pulse"
    )
    return pulse
}

@Composable
private fun maxParticleDrift(): Float {
    val transition = rememberInfiniteTransition(label = "effort-particles")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing)),
        label = "effort-drift"
    )
    return drift
}

private fun DrawScope.drawMaxGlow(pulse: Float, color: androidx.compose.ui.graphics.Color, center: Offset = this.center) {
    drawCircle(color.copy(alpha = 0.10f * pulse), radius = 22.dp.toPx(), center = center)
    drawCircle(color.copy(alpha = 0.22f * pulse), radius = 14.dp.toPx(), center = center)
}

private fun DrawScope.drawMaxParticles(phase: Float, color: androidx.compose.ui.graphics.Color, origin: Offset) {
    repeat(7) { index ->
        val local = (phase + index / 7f) % 1f
        val angle = (index * 51f) * (Math.PI.toFloat() / 180f)
        val distance = 10.dp.toPx() + local * 16.dp.toPx()
        val point = Offset(
            origin.x + kotlin.math.cos(angle) * distance,
            origin.y - local * 18.dp.toPx()
        )
        drawCircle(color.copy(alpha = (1f - local) * 0.75f), radius = (2.4f - local).coerceAtLeast(0.6f).dp.toPx(), center = point)
    }
}

/**
 * 上下文详情：本次请求预计占用、预算构成、模型实际用量，并可直接改上下文长度。
 * [onCustomize] 打开精确数值对话框，[onReset] 恢复默认（不设上限时的服务端行为）。
 *
 * 点选窗口档位由调用方负责立即生效并收起面板，这里只负责把选中态如实反映出来。
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
                    Text(
                        if (budget > 0) "$totalTokens / $budget" else "$totalTokens",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
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
                    if (limits == null) "未设上下文长度" else
                        "窗口 ${ContextWindowPresets.label(limits.windowTokens)} · 输出上限 ${limits.outputTokens} · 余量 ${limits.safetyTokens}",
                    color = MutedInk,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ContextBreakLine("系统提示词", systemTokens)
            ContextBreakLine("对话历史", historyTokens)
            ContextBreakLine("图片附件", attachmentTokens)
            ContextBreakLine("请求开销", overheadTokens)
            if (omittedTurns > 0) {
                Text(
                    "已省略最早 $omittedTurns 轮",
                    color = Danger,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        if (lastRequestInputTokens > 0 || lastRequestOutputTokens > 0) {
            Text(
                "上次请求 输入 $lastRequestInputTokens · 输出 $lastRequestOutputTokens" +
                    if (lastRequestReasoningTokens > 0) " · 含推理 $lastRequestReasoningTokens" else "",
                color = MutedInk,
                style = MaterialTheme.typography.bodyMedium
            )
        }

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
    val haptics = LocalHapticFeedback.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ContextWindowPresets.values.forEach { (window, label) ->
            val selected = limits?.windowTokens == window
            Surface(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSelect(window)
                },
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
                    }
                }
            }
        }
    }
}

@Composable
private fun ContextBreakLine(label: String, tokens: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, color = MutedInk, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(
            "$tokens",
            color = if (tokens > 0) Ink else MutedInk,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
