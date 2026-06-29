package com.heyanle.easy_bangumi_cm.common.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Created by HeYanLe on 2023/2/18 23:03.
 * https://github.com/heyanLE
 */
object StrawberryColor {
    object Light {
        val primary = Color(0xFFB61E40)
        val onPrimary = Color(0xFFFFFFFF)
        val primaryContainer = Color(0xFFFFDADD)
        val onPrimaryContainer = Color(0xFF40000D)
        val secondary = Color(0xFFB61E40)
        val onSecondary = Color(0xFFFFFFFF)
        val secondaryContainer = Color(0xFFFFDADD)
        val onSecondaryContainer = Color(0xFF40000D)
        val tertiary = Color(0xFF775930)
        val onTertiary = Color(0xFFFFFFFF)
        val tertiaryContainer = Color(0xFFFFDDB1)
        val onTertiaryContainer = Color(0xFF2A1800)
        val background = Color(0xFFFCFCFC)
        val onBackground = Color(0xFF201A1A)
        val surface = Color(0xFFFCFCFC)
        val onSurface = Color(0xFF201A1A)
        val surfaceVariant = Color(0xFFF4DDDD)
        val onSurfaceVariant = Color(0xFF534344)
        val outline = Color(0xFF857374)
        val inverseOnSurface = Color(0xFFFBEDED)
        val inverseSurface = Color(0xFF362F2F)
        val primaryInverse = Color(0xFFFFB2B9)

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
        val primary = Color(0xFFFFB2B9)
        val onPrimary = Color(0xFF67001B)
        val primaryContainer = Color(0xFF91002A)
        val onPrimaryContainer = Color(0xFFFFDADD)
        val secondary = Color(0xFFFFB2B9)
        val onSecondary = Color(0xFF67001B)
        val secondaryContainer = Color(0xFF91002A)
        val onSecondaryContainer = Color(0xFFFFDADD)
        val tertiary = Color(0xFFE8C08E)
        val onTertiary = Color(0xFF432C06)
        val tertiaryContainer = Color(0xFF5D421B)
        val onTertiaryContainer = Color(0xFFFFDDB1)
        val background = Color(0xFF201A1A)
        val onBackground = Color(0xFFECDFDF)
        val surface = Color(0xFF201A1A)
        val onSurface = Color(0xFFECDFDF)
        val surfaceVariant = Color(0xFF534344)
        val onSurfaceVariant = Color(0xFFD7C1C2)
        val outline = Color(0xFFA08C8D)
        val inverseOnSurface = Color(0xFF201A1A)
        val inverseSurface = Color(0xFFECDFDF)
        val primaryInverse = Color(0xFFB61E40)

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
