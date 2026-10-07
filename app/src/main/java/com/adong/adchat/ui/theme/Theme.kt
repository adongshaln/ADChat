package com.adong.adchat.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.adong.adchat.R
import com.adong.adchat.data.THEME_MODE_ASTER
import com.adong.adchat.data.THEME_MODE_CLAUDE

/**
 * 一套主题的全部颜色。界面代码只认这里的语义名，切换主题=切换整个调色板实例。
 * 新增主题时补一个调色板常量并在 AsterTheme 的 when 里登记即可。
 */
@Immutable
data class AsterPalette(
    val canvas: Color,
    val surface: Color,
    val ink: Color,
    val mutedInk: Color,
    val hairline: Color,
    val accent: Color,
    val accentSoft: Color,
    val sage: Color,
    val sageSoft: Color,
    val surfaceInset: Color,
    val warmWhite: Color,
    val danger: Color,
    val dangerSoft: Color,
    val night: Color,
    val quoteAmber: Color,
    val bracketBlue: Color,
    val amberSoft: Color
)

/** Aster 原生暖米色调。 */
val AsterPaletteLight = AsterPalette(
    canvas = Color(0xFFF6F3EE),
    surface = Color(0xFFFFFDFA),
    ink = Color(0xFF302C28),
    mutedInk = Color(0xFF787169),
    hairline = Color(0xFFE6E0D7),
    accent = Color(0xFF8B5E4B),
    accentSoft = Color(0xFFF0E5DC),
    sage = Color(0xFF586B56),
    sageSoft = Color(0xFFE8EDE3),
    surfaceInset = Color(0xFFEEE9E1),
    warmWhite = Color(0xFFFFF9F0),
    danger = Color(0xFFB33A32),
    dangerSoft = Color(0xFFFFE8E5),
    night = Color(0xFF352F2A),
    quoteAmber = Color(0xFF9A6B12),
    bracketBlue = Color(0xFF6292B3),
    amberSoft = Color(0xFFFFF1D8)
)

/** Claude 风格：暖象牙底、陶土橘强调（取样自官方浅色界面，见设计文档）。 */
val ClaudePaletteLight = AsterPalette(
    canvas = Color(0xFFFAF9F5),
    surface = Color(0xFFFFFFFF),
    ink = Color(0xFF1F1E1D),
    mutedInk = Color(0xFF87867F),
    hairline = Color(0xFFE6E2D9),
    accent = Color(0xFFD97757),
    accentSoft = Color(0xFFF4E8E1),
    sage = Color(0xFF586B56),
    sageSoft = Color(0xFFE8EDE3),
    surfaceInset = Color(0xFFF0EEE6),
    warmWhite = Color(0xFFF5F4EF),
    danger = Color(0xFFC42B1C),
    dangerSoft = Color(0xFFFFE8E5),
    night = Color(0xFF1F1E1D),
    quoteAmber = Color(0xFF9A6B12),
    bracketBlue = Color(0xFF6292B3),
    amberSoft = Color(0xFFF5EBD9)
)

val LocalAsterPalette = staticCompositionLocalOf { AsterPaletteLight }

// 兼容层：沿用历史常量名，取值来自当前调色板。只能在组合上下文读取。
val Canvas @Composable get() = LocalAsterPalette.current.canvas
val Surface @Composable get() = LocalAsterPalette.current.surface
val Ink @Composable get() = LocalAsterPalette.current.ink
val MutedInk @Composable get() = LocalAsterPalette.current.mutedInk
val Hairline @Composable get() = LocalAsterPalette.current.hairline
val Accent @Composable get() = LocalAsterPalette.current.accent
val AccentSoft @Composable get() = LocalAsterPalette.current.accentSoft
val Sage @Composable get() = LocalAsterPalette.current.sage
val SageSoft @Composable get() = LocalAsterPalette.current.sageSoft
val SurfaceInset @Composable get() = LocalAsterPalette.current.surfaceInset
val WarmWhite @Composable get() = LocalAsterPalette.current.warmWhite
val Danger @Composable get() = LocalAsterPalette.current.danger
val DangerSoft @Composable get() = LocalAsterPalette.current.dangerSoft
val Night @Composable get() = LocalAsterPalette.current.night
val QuoteAmber @Composable get() = LocalAsterPalette.current.quoteAmber
val BracketBlue @Composable get() = LocalAsterPalette.current.bracketBlue
val AmberSoft @Composable get() = LocalAsterPalette.current.amberSoft

private fun AsterPalette.toColorScheme() = lightColorScheme(
    primary = ink,
    onPrimary = warmWhite,
    primaryContainer = accentSoft,
    onPrimaryContainer = ink,
    secondary = accent,
    onSecondary = Color.White,
    secondaryContainer = accentSoft,
    onSecondaryContainer = accent,
    tertiary = sage,
    tertiaryContainer = sageSoft,
    onTertiaryContainer = sage,
    background = canvas,
    onBackground = ink,
    surface = surface,
    onSurface = ink,
    surfaceVariant = surfaceInset,
    onSurfaceVariant = mutedInk,
    outline = hairline,
    outlineVariant = hairline,
    surfaceTint = Color.Transparent,
    surfaceContainer = canvas,
    surfaceContainerLow = surface,
    surfaceContainerHigh = surfaceInset,
    error = danger,
    errorContainer = dangerSoft
)

private val typography = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 32.sp, lineHeight = 43.sp, letterSpacing = (-0.8).sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 28.sp, lineHeight = 37.sp, letterSpacing = (-0.6).sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 21.sp, lineHeight = 29.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 21.sp),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 27.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
    ),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp)
)

private val shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp), large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * Claude 主题的衬线标题字族（Source Serif 4，OFL 协议，见 docs/fonts/）。
 * 可变字体按字重出实例；中文回退系统衬线，属渐进增强。
 */
@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
val SerifHeading = FontFamily(
    Font(R.font.sourceserif4_variable, weight = FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.sourceserif4_variable, weight = FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.sourceserif4_variable, weight = FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.sourceserif4_variable, weight = FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700)))
)

private fun Typography.withSerifHeadings() = copy(
    displaySmall = displaySmall.copy(fontFamily = SerifHeading, letterSpacing = 0.sp),
    headlineMedium = headlineMedium.copy(fontFamily = SerifHeading, letterSpacing = 0.sp),
    titleLarge = titleLarge.copy(fontFamily = SerifHeading)
)

private val claudeTypography = typography.withSerifHeadings()

private val claudeShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp), small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp), large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun AsterTheme(themeMode: String = THEME_MODE_ASTER, content: @Composable () -> Unit) {
    val claude = themeMode == THEME_MODE_CLAUDE
    val palette = if (claude) ClaudePaletteLight else AsterPaletteLight
    CompositionLocalProvider(LocalAsterPalette provides palette) {
        MaterialTheme(
            colorScheme = palette.toColorScheme(),
            typography = if (claude) claudeTypography else typography,
            shapes = if (claude) claudeShapes else shapes,
            content = content
        )
    }
}
