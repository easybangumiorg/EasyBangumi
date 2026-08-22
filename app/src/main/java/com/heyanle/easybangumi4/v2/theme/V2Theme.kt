package com.heyanle.easybangumi4.v2.theme

import android.app.UiModeManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.heyanle.inject.core.Inject

@Immutable
data class V2ColorPalette(
    val accent: Color,
    val onAccent: Color,
    val accentContainer: Color,
    val onAccentContainer: Color,
    val background: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val iconPrimary: Color,
    val iconSecondary: Color,
    val divider: Color,
    val error: Color,
    val immersiveAccent: Color,
    val onImmersiveAccent: Color,
    val immersiveAccentContainer: Color,
    val onImmersiveAccentContainer: Color,
    val isDark: Boolean,
)

private fun V2ThemeColor.toPalette(isDark: Boolean): V2ColorPalette {
    val themeColors = colors(isDark)
    val immersiveColors = colors(isDark = true)
    return V2ColorPalette(
        accent = themeColors.accent,
        onAccent = themeColors.onAccent,
        accentContainer = themeColors.accentContainer,
        onAccentContainer = themeColors.onAccentContainer,
        background = if (isDark) Color(0xFF121212) else Color(0xFFFBFAF7),
        surface = if (isDark) Color(0xFF1C1C1E) else Color(0xFFFFFEFC),
        surfaceMuted = if (isDark) Color(0xFF28282B) else Color(0xFFF3F1ED),
        textPrimary = if (isDark) Color(0xFFF5F3F0) else Color(0xFF171614),
        textSecondary = if (isDark) Color(0xFFB8B4AE) else Color(0xFF716E68),
        iconPrimary = if (isDark) Color(0xFFE9E6E1) else Color(0xFF2A2927),
        iconSecondary = if (isDark) Color(0xFFC5C1BB) else Color(0xFF716E68),
        divider = if (isDark) Color(0xFF3A393D) else Color(0xFFE4E0D9),
        error = if (isDark) Color(0xFFFFB4AB) else Color(0xFFC62828),
        immersiveAccent = immersiveColors.accent,
        onImmersiveAccent = immersiveColors.onAccent,
        immersiveAccentContainer = immersiveColors.accentContainer,
        onImmersiveAccentContainer = immersiveColors.onAccentContainer,
        isDark = isDark,
    )
}

private val LocalV2ColorPalette = staticCompositionLocalOf {
    V2ThemeColor.BrandYellow.toPalette(isDark = false)
}

/** Semantic V2 colors. Components should use these names instead of a concrete yellow token. */
object V2Theme {
    val colors: V2ColorPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalV2ColorPalette.current
}

@Composable
fun V2ThemeProvider(content: @Composable () -> Unit) {
    val controller: V2ThemeController by Inject.injectLazy()
    val themeState by controller.themeFlow.collectAsState()
    val context = LocalContext.current
    val configurationDark = isSystemInDarkTheme()
    val systemNightMode = context.getSystemService(UiModeManager::class.java)?.nightMode
    // The legacy activity may override the local AppCompat configuration. Reading UiModeManager
    // keeps V2 tied to the device setting even when that old, activity-scoped preference is Light.
    val isDark = when (systemNightMode) {
        UiModeManager.MODE_NIGHT_YES -> true
        UiModeManager.MODE_NIGHT_NO -> false
        else -> configurationDark
    }
    val palette = themeState.themeColor.toPalette(isDark)
    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = palette.onAccent,
            primaryContainer = palette.accentContainer,
            onPrimaryContainer = palette.onAccentContainer,
            secondary = palette.accent,
            onSecondary = palette.onAccent,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surfaceMuted,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.divider,
            error = palette.error,
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = palette.onAccent,
            primaryContainer = palette.accentContainer,
            onPrimaryContainer = palette.onAccentContainer,
            secondary = palette.accent,
            onSecondary = palette.onAccent,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surfaceMuted,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.divider,
            error = palette.error,
        )
    }
    CompositionLocalProvider(LocalV2ColorPalette provides palette) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}
