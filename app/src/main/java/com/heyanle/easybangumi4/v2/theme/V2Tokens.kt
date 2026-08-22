package com.heyanle.easybangumi4.v2.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Stable V2 presentation tokens. Legacy screens continue to use the existing app theme. */
object V2Tokens {
    val WarmBackground: Color
        @Composable @ReadOnlyComposable get() = V2Theme.colors.background
    val Surface: Color
        @Composable @ReadOnlyComposable get() = V2Theme.colors.surface
    val SurfaceMuted: Color
        @Composable @ReadOnlyComposable get() = V2Theme.colors.surfaceMuted
    val TextPrimary: Color
        @Composable @ReadOnlyComposable get() = V2Theme.colors.textPrimary
    val TextSecondary: Color
        @Composable @ReadOnlyComposable get() = V2Theme.colors.textSecondary
    val IconPrimary: Color
        @Composable @ReadOnlyComposable get() = V2Theme.colors.textPrimary
    val IconSecondary: Color
        @Composable @ReadOnlyComposable get() = V2Theme.colors.textSecondary
    val Divider: Color
        @Composable @ReadOnlyComposable get() = V2Theme.colors.divider
    val Error: Color
        @Composable @ReadOnlyComposable get() = V2Theme.colors.error
    val PlayerDark = Color(0xFF121212)
    val PlayerSurface = Color(0xFF1C1C1E)
    val PlayerSurfaceMuted = Color(0xFF28282B)
    val PlayerDivider = Color(0xFF303033)
    val PlayerTextPrimary = Color(0xFFF5F3F0)
    val PlayerTextSecondary = Color(0xFFB3B3B3)
    val PlayerError = Color(0xFFFFB4AB)

    val ScreenHorizontalPadding = 16.dp
    val TabSpacing = 28.dp
    val TabIndicatorWidth = 24.dp
    val MinimumTouchTarget = 44.dp
}
