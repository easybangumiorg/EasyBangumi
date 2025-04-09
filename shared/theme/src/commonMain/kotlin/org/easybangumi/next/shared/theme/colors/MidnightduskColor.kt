package com.heyanle.easy_bangumi_cm.common.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Created by HeYanLe on 2023/2/18 22:59.
 * https://github.com/heyanLE
 */

object MidnightduskColor {
    object Light {
        val primary = Color(0xFFBB0054)
        val onPrimary = Color(0xFFFFFFFF)
        val primaryContainer = Color(0xFFFFD9E1)
        val onPrimaryContainer = Color(0xFF3F0017)
        val secondary = Color(0xFFBB0054)
        val onSecondary = Color(0xFFFFFFFF)
        val secondaryContainer = Color(0xFFFFD9E1)
        val onSecondaryContainer = Color(0xFF3F0017)
        val tertiary = Color(0xFF006638)
        val onTertiary = Color(0xFFFFFFFF)
        val tertiaryContainer = Color(0xFF00894b)
        val onTertiaryContainer = Color(0xFF2D1600)
        val background = Color(0xFFFFFBFF)
        val onBackground = Color(0xFF1C1B1F)
        val surface = Color(0xFFFFFBFF)
        val onSurface = Color(0xFF1C1B1F)
        val surfaceVariant = Color(0xFFF3DDE0)
        val onSurfaceVariant = Color(0xFF524346)
        val outline = Color(0xFF847376)
        val inverseOnSurface = Color(0xFFF4F0F4)
        val inverseSurface = Color(0xFF313033)
        val primaryInverse = Color(0xFFFFB1C4)
        val elevationOverlay = primary

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
        val primary = Color(0xFFF02475)
        val onPrimary = Color(0xFFFFFFFF)
        val primaryContainer = Color(0xFFBD1C5C)
        val onPrimaryContainer = Color(0xFFFFFFFF)
        val secondary = Color(0xFFF02475)
        val onSecondary = Color(0xFFFFFFFF)
        val secondaryContainer = Color(0xFFF02475)
        val onSecondaryContainer = Color(0xFFFFFFFF)
        val tertiary = Color(0xFF55971C)
        val onTertiary = Color(0xFFFFFFFF)
        val tertiaryContainer = Color(0xFF386412)
        val onTertiaryContainer = Color(0xFFE5E1E5)
        val background = Color(0xFF16151D)
        val onBackground = Color(0xFFE5E1E5)
        val surface = Color(0xFF16151D)
        val onSurface = Color(0xFFE5E1E5)
        val surfaceVariant = Color(0xFF524346)
        val onSurfaceVariant = Color(0xFFD6C1C4)
        val outline = Color(0xFF9F8C8F)
        val inverseSurface = Color(0xFF333043)
        val inverseOnSurface = Color(0xFFFFFFFF)
        val primaryInverse = Color(0xFFF02475)
        val elevationOverlay = Color(0xFF2C0013)

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



