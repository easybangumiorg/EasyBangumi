package com.heyanle.easybangumi4.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Created by HeYanLe on 2023/2/18 23:11.
 * https://github.com/heyanLE
 */
object TealturqoiseColor {
    
    object Light {
        val primary = Color(0xFF008080)
        val onPrimary = Color(0xFFFFFFFF)
        val primaryContainer = Color(0xFF008080)
        val onPrimaryContainer = Color(0xFFFFFFFF)
        val secondary = Color(0xFF008080)
        val onSecondary = Color(0xFFFFFFFF)
        val secondaryContainer = Color(0xFFBFDFDF)
        val onSecondaryContainer = Color(0xFF008080)
        val tertiary = Color(0xFFFF7F7F)
        val onTertiary = Color(0xFF000000)
        val tertiaryContainer = Color(0xFF2A1616)
        val onTertiaryContainer = Color(0xFFFF7F7F)
        val background = Color(0xFFFAFAFA)
        val onBackground = Color(0xFF050505)
        val surface = Color(0xFFFAFAFA)
        val onSurface = Color(0xFF050505)
        val surfaceVariant = Color(0xFFDAE5E2)
        val onSurfaceVariant = Color(0xFF050505)
        val outline = Color(0xFF6F7977)
        val inverseOnSurface = Color(0xFFFAFAFA)
        val inverseSurface = Color(0xFF050505)
        val primaryInverse = Color(0xFF40E0D0)
        val elevationOverlay = Color(0xFFBFDFDF)

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
        val primary = Color(0xFF40E0D0)
        val onPrimary = Color(0xFF000000)
        val primaryContainer = Color(0xFF40E0D0)
        val onPrimaryContainer = Color(0xFF000000)
        val secondary = Color(0xFF40E0D0)
        val onSecondary = Color(0xFF000000)
        val secondaryContainer = Color(0xFF18544E)
        val onSecondaryContainer = Color(0xFF40E0D0)
        val tertiary = Color(0xFFBF1F2F)
        val onTertiary = Color(0xFFFFFFFF)
        val tertiaryContainer = Color(0xFF200508)
        val onTertiaryContainer = Color(0xFFBF1F2F)
        val background = Color(0xFF202125)
        val onBackground = Color(0xFFDFDEDA)
        val surface = Color(0xFF202125)
        val onSurface = Color(0xFFDFDEDA)
        val surfaceVariant = Color(0xFF3F4947)
        val onSurfaceVariant = Color(0xFFDFDEDA)
        val outline = Color(0xFF899391)
        val inverseOnSurface = Color(0xFF202125)
        val inverseSurface = Color(0xFFDFDEDA)
        val primaryInverse = Color(0xFF008080)
        val elevationOverlay = Color(0xFF18544E)

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