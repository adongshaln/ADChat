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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.adong.adchat.data.THEME_MODE_ASTER
import com.adong.adchat.data.THEME_MODE_GLASS

/**
 * 一套主题的全部颜色。界面代码只认这里的语义名，切换主题=切换调色板实例。
 * 液态玻璃主题沿用同一套基础配色——玻璃只改 chrome 层的材质，不改内容色板。
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
    val bracketBlue: Color
)

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
    bracketBlue = Color(0xFF6292B3)
)

val LocalAsterPalette = staticCompositionLocalOf { AsterPaletteLight }

/** 当前主题模式，玻璃材质据此开关。 */
val LocalThemeMode = staticCompositionLocalOf { THEME_MODE_ASTER }

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

@Composable
fun AsterTheme(themeMode: String = THEME_MODE_ASTER, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalAsterPalette provides AsterPaletteLight,
        LocalThemeMode provides themeMode
    ) {
        MaterialTheme(
            colorScheme = AsterPaletteLight.toColorScheme(),
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
}
