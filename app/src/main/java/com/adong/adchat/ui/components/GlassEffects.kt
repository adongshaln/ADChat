package com.adong.adchat.ui.components

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.adong.adchat.data.THEME_MODE_GLASS
import com.adong.adchat.ui.theme.LocalAsterPalette
import com.adong.adchat.ui.theme.LocalThemeMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

/** 屏幕级共享的玻璃背板状态；非玻璃主题或旧系统上为 null，所有玻璃修饰符自动退化为 no-op。 */
val LocalGlassHazeState = staticCompositionLocalOf<HazeState?> { null }

/**
 * 液态玻璃材质（借鉴 BiliPai 的 Haze 用法）：
 * 滚动内容用 [glassSource] 供背板，chrome 表面用 [glassSurface] 取毛玻璃。
 * 仅 Android 12+ 有真实模糊；旧系统上 Haze 自动退化为不透明染色，
 * 所以玻璃模式下 chrome 仍需给出接近实底的底色，保证可读性。
 */

@Composable
fun rememberGlassHazeState(): HazeState? {
    val enabled = LocalThemeMode.current == THEME_MODE_GLASS && Build.VERSION.SDK_INT >= 31
    return remember(enabled) { if (enabled) HazeState() else null }
}

/** 背板内容源：玻璃关闭时是 no-op。 */
fun Modifier.glassSource(state: HazeState?): Modifier =
    if (state == null) this else this.hazeSource(state)

/**
 * 玻璃 chrome 表面：自身裁成 [shape]，把背板模糊后垫在内容下面。
 * [baseColor] 是玻璃底色（作为染色与旧系统回退），玻璃关闭时为 no-op。
 */
@Composable
fun Modifier.glassSurface(state: HazeState?, shape: RoundedCornerShape, baseColor: Color): Modifier {
    if (state == null) return this
    val canvas = LocalAsterPalette.current.canvas
    // 顶部亮、底部透的纵向染色，给出「液态」高光；纯色会让玻璃看起来像换个底色。
    val style = HazeStyle(
        backgroundColor = canvas,
        tints = listOf(
            HazeTint(
                Brush.verticalGradient(
                    listOf(baseColor.copy(alpha = .74f), baseColor.copy(alpha = .42f))
                )
            )
        ),
        blurRadius = 24.dp
    )
    return this.clip(shape).hazeEffect(state = state, style = style)
}
