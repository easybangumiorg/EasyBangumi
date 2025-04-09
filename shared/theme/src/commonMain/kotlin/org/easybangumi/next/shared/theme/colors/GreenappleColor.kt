package com.heyanle.easy_bangumi_cm.common.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Created by HeYanLe on 2023/2/18 22:54.
 * https://github.com/heyanLE
 */
object GreenappleColor {
    object Light {
        val primary = Color(0xFF006D2F)
        val onPrimary = Color(0xFFFFFFFF)
        val primaryContainer = Color(0xFF96F8A9)
        val onPrimaryContainer = Color(0xFF002109)
        val secondary = Color(0xFF006D2F)
        val onSecondary = Color(0xFFFFFFFF)
        val secondaryContainer = Color(0xFF96F8A9)
        val onSecondaryContainer = Color(0xFF002109)
        val tertiary = Color(0xFFB91D22)
        val onTertiary = Color(0xFFFFFFFF)
        val tertiaryContainer = Color(0xFFFFDAD5)
        val onTertiaryContainer = Color(0xFF410003)
        val background = Color(0xFFFBFDF7)
        val onBackground = Color(0xFF1A1C19)
        val surface = Color(0xFFFBFDF7)
        val onSurface = Color(0xFF1A1C19)
        val surfaceVariant = Color(0xFFDDE5DA)
        val onSurfaceVariant = Color(0xFF414941)
        val outline = Color(0xFF717970)
        val inverseOnSurface = Color(0xFFF0F2EC)
        val inverseSurface = Color(0xFF2F312E)
        val primaryInverse = Color(0xFF7ADB8F)
        
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
            inversePrimary = primaryInverse

        )
    }
    
    object Dark {
        val primary = Color(0xFF7ADB8F)
        val onPrimary = Color(0xFF003915)
        val primaryContainer = Color(0xFF005322)
        val onPrimaryContainer = Color(0xFF96F8A9)
        val secondary = Color(0xFF7ADB8F)
        val onSecondary = Color(0xFF003915)
        val secondaryContainer = Color(0xFF005322)
        val onSecondaryContainer = Color(0xFF96F8A9)
        val tertiary = Color(0xFFFFB3AA)
        val onTertiary = Color(0xFF680006)
        val tertiaryContainer = Color(0xFF93000D)
        val onTertiaryContainer = Color(0xFFFFDAD5)
        val background = Color(0xFF1A1C19)
        val onBackground = Color(0xFFE1E3DD)
        val surface = Color(0xFF1A1C19)
        val onSurface = Color(0xFFE1E3DD)
        val surfaceVariant = Color(0xFF414941)
        val onSurfaceVariant = Color(0xFFC1C8BE)
        val outline = Color(0xFF8B9389)
        val inverseOnSurface = Color(0xFF1A1C19)
        val inverseSurface = Color(0xFFE1E3DD)
        val primaryInverse = Color(0xFF006D2F)

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
            inversePrimary = primaryInverse

        )
    }
}


