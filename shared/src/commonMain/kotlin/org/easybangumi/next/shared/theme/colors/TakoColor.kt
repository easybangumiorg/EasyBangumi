package com.heyanle.easy_bangumi_cm.common.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Created by HeYanLe on 2023/2/18 23:09.
 * https://github.com/heyanLE
 */
object TakoColor {
    
    object Light {
        val primary = Color(0xFF66577E)
        val onPrimary = Color(0xFFF3B375)
        val primaryContainer = Color(0xFF66577E)
        val onPrimaryContainer = Color(0xFFF3B375)
        val secondary = Color(0xFF66577E)
        val onSecondary = Color(0xFFF3B375)
        val secondaryContainer = Color(0xFF66577E)
        val onSecondaryContainer = Color(0xFFF3B375)
        val tertiary = Color(0xFFF3B375)
        val onTertiary = Color(0xFF574360)
        val tertiaryContainer = Color(0xFFFDD6B0)
        val onTertiaryContainer = Color(0xFF221437)
        val background = Color(0xFFF7F5FF)
        val onBackground = Color(0xFF1B1B22)
        val surface = Color(0xFFF7F5FF)
        val onSurface = Color(0xFF1B1B22)
        val surfaceVariant = Color(0xFFE8E0EB)
        val onSurfaceVariant = Color(0xFF49454E)
        val outline = Color(0xFF7A757E)
        val inverseOnSurface = Color(0xFFF3EFF4)
        val inverseSurface = Color(0xFF313033)
        val primaryInverse = Color(0xFFD6BAFF)
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
        val primary = Color(0xFFF3B375)
        val onPrimary = Color(0xFF38294E)
        val primaryContainer = Color(0xFFF3B375)
        val onPrimaryContainer = Color(0xFF38294E)
        val secondary = Color(0xFFF3B375)
        val onSecondary = Color(0xFF38294E)
        val secondaryContainer = Color(0xFFF3B375)
        val onSecondaryContainer = Color(0xFF38294E)
        val tertiary = Color(0xFF66577E)
        val onTertiary = Color(0xFFF3B375)
        val tertiaryContainer = Color(0xFF4E4065)
        val onTertiaryContainer = Color(0xFFEDDCFF)
        val background = Color(0xFF21212E)
        val onBackground = Color(0xFFE3E0F2)
        val surface = Color(0xFF21212E)
        val onSurface = Color(0xFFE3E0F2)
        val surfaceVariant = Color(0xFF49454E)
        val onSurfaceVariant = Color(0xFFCBC4CE)
        val outline = Color(0xFF958F99)
        val inverseOnSurface = Color(0xFF1B1B1E)
        val inverseSurface = Color(0xFFE5E1E6)
        val primaryInverse = Color(0xFF84531E)
        val elevationOverlay = primary
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