package com.heyanle.easy_bangumi_cm.common.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Created by HeYanLe on 2023/2/18 23:08.
 * https://github.com/heyanLE
 */
object DefaultColor {
    
    object Light {
        val primary = Color(0xFF0057CE)
        val onPrimary = Color(0xFFFFFFFF)
        val primaryContainer = Color(0xFFD8E2FF)
        val onPrimaryContainer = Color(0xFF001947)
        val secondary = Color(0xFF0057CE)
        val onSecondary = Color(0xFFFFFFFF)
        val secondaryContainer = Color(0xFFD8E2FF)
        val onSecondaryContainer = Color(0xFF001947)
        val tertiary = Color(0xFF006E17)
        val onTertiary = Color(0xFFFFFFFF)
        val tertiaryContainer = Color(0xFF95F990)
        val onTertiaryContainer = Color(0xFF002202)
        val background = Color(0xFFFDFBFF)
        val onBackground = Color(0xFF1B1B1E)
        val surface = Color(0xFFFDFBFF)
        val onSurface = Color(0xFF1B1B1E)
        val surfaceVariant = Color(0xFFE1E2EC)
        val onSurfaceVariant = Color(0xFF44464E)
        val outline = Color(0xFF757780)
        val inverseOnSurface = Color(0xFFF2F0F4)
        val inverseSurface = Color(0xFF303033)
        val primaryInverse = Color(0xFFAEC6FF)

        val colorScheme = lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            inverseOnSurface = inverseOnSurface,
            inverseSurface = inverseSurface,
            inversePrimary = primaryInverse,
        )
    }
    
    object Dark {
        val primary = Color(0xFFAEC6FF)
        val onPrimary = Color(0xFF002C71)
        val primaryContainer = Color(0xFF00419E)
        val onPrimaryContainer = Color(0xFFD8E2FF)
        val secondary = Color(0xFFAEC6FF)
        val onSecondary = Color(0xFF002C71)
        val secondaryContainer = Color(0xFF00419E)
        val onSecondaryContainer = Color(0xFFD8E2FF)
        val tertiary = Color(0xFF7ADC77)
        val onTertiary = Color(0xFF003907)
        val tertiaryContainer = Color(0xFF00530D)
        val onTertiaryContainer = Color(0xFF95F990)
        val background = Color(0xFF1B1B1E)
        val onBackground = Color(0xFFE4E2E6)
        val surface = Color(0xFF1B1B1E)
        val onSurface = Color(0xFFE4E2E6)
        val surfaceVariant = Color(0xFF44464E)
        val onSurfaceVariant = Color(0xFFC5C6D0)
        val outline = Color(0xFF8E9099)
        val inverseOnSurface = Color(0xFF1B1B1E)
        val inverseSurface = Color(0xFFE4E2E6)
        val primaryInverse = Color(0xFF0057CE)

        val colorScheme = darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            inverseOnSurface = inverseOnSurface,
            inverseSurface = inverseSurface,
            inversePrimary = primaryInverse,
        )
    }
    
}