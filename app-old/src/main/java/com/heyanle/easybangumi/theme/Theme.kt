package com.heyanle.easybangumi.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

/**
 * Created by HeYanLe on 2023/1/7 12:35.
 * https://github.com/heyanLE
 */

enum class EasyThemeMode(
    val darkColorScheme: ColorScheme,
    val lightColorScheme: ColorScheme,
) {
    Blue(
        DarkBlueColorScheme,
        LightBlueColorScheme
    ),
    Green(
        DarkGreenColorScheme,
        LightGreenColorScheme
    ),
    Orange(
        DarkOrangeColorScheme,
        LightOrangeColorScheme,
    ),
    Pink(
        DarkPinkColorScheme,
        LightPinkColorScheme,
    ),
    Purple(
        DarkPurpleColorScheme,
        LightPurpleColorScheme,
    ),
    Yellow(
        DarkYellowColorScheme,
        LightYellowColorScheme
    )
}

fun EasyThemeMode.getColorScheme(isDark: Boolean): ColorScheme {
    return if (isDark) darkColorScheme else lightColorScheme
}

private val DarkGreenColorScheme = darkColorScheme(
    primary = GreenDarkPrimary,
    secondary = GreenDarkSecondary,
    background = GreenDarkBackground,
    surface = GreenDarkSurface,
    onPrimary = DarkOnPrimary,
    onSecondary = DarkOnSecondary,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
)

private val DarkBlueColorScheme = darkColorScheme(
    primary = BlueDarkPrimary,
    secondary = BlueDarkSecondary,
    background = BlueDarkBackground,
    surface = BlueDarkSurface,
    onPrimary = DarkOnPrimary,
    onSecondary = DarkOnSecondary,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
)

private val DarkOrangeColorScheme = darkColorScheme(
    primary = OrangeDarkPrimary,
    secondary = OrangeDarkSecondary,
    background = OrangeDarkBackground,
    surface = OrangeDarkSurface,
    onPrimary = DarkOnPrimary,
    onSecondary = DarkOnSecondary,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
)

private val DarkPinkColorScheme = darkColorScheme(
    primary = PinkDarkPrimary,
    secondary = PinkDarkSecondary,
    background = PinkDarkBackground,
    surface = PinkDarkSurface,
    onPrimary = DarkOnPrimary,
    onSecondary = DarkOnSecondary,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
)

private val DarkPurpleColorScheme = darkColorScheme(
    primary = PurpleDarkPrimary,
    secondary = PurpleDarkSecondary,
    background = PurpleDarkBackground,
    surface = PurpleDarkSurface,
    onPrimary = DarkOnPrimary,
    onSecondary = DarkOnSecondary,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
)

private val DarkYellowColorScheme = darkColorScheme(
    primary = YellowDarkPrimary,
    secondary = YellowDarkSecondary,
    background = YellowDarkBackground,
    surface = YellowDarkSurface,
    onPrimary = DarkOnPrimary,
    onSecondary = DarkOnSecondary,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
)

private val LightGreenColorScheme = lightColorScheme(
    primary = GreenLightPrimary,
    secondary = GreenLightSecondary,
    onPrimary = LightOnPrimary,
    onSecondary = LightOnSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
)

private val LightBlueColorScheme = lightColorScheme(
    primary = BlueLightPrimary,
    secondary = BlueLightSecondary,
    onPrimary = LightOnPrimary,
    onSecondary = LightOnSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
)

private val LightOrangeColorScheme = lightColorScheme(
    primary = OrangeLightPrimary,
    secondary = OrangeLightSecondary,
    onPrimary = LightOnPrimary,
    onSecondary = LightOnSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
)

private val LightPinkColorScheme = lightColorScheme(
    primary = PinkLightPrimary,
    secondary = PinkLightSecondary,
    onPrimary = LightOnPrimary,
    onSecondary = LightOnSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
)

private val LightPurpleColorScheme = lightColorScheme(
    primary = PurpleLightPrimary,
    secondary = PurpleLightSecondary,
    onPrimary = LightOnPrimary,
    onSecondary = LightOnSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
)

private val LightYellowColorScheme = lightColorScheme(
    primary = YellowLightPrimary,
    secondary = YellowLightSecondary,
    onPrimary = LightOnPrimary,
    onSecondary = LightOnSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
)