package com.heyanle.easybangumi4.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.heyanle.easybangumi4.theme.colors.GreenappleColor
import com.heyanle.easybangumi4.theme.colors.MidnightduskColor
import com.heyanle.easybangumi4.theme.colors.StrawberryColor
import com.heyanle.easybangumi4.theme.colors.TachiyomiColor
import com.heyanle.easybangumi4.theme.colors.TakoColor
import com.heyanle.easybangumi4.theme.colors.TealturqoiseColor


/**
 * Created by HeYanLe on 2023/2/18 22:47.
 * https://github.com/heyanLE
 */

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

fun EasyThemeMode.getColorScheme(isDark: Boolean): ColorScheme {
    return if (isDark) darkColorScheme else lightColorScheme
}
enum class EasyThemeMode(
    val darkColorScheme: ColorScheme,
    val lightColorScheme: ColorScheme,
    val darkElevationOverlay: Color = Color.Unspecified,
    val lightElevationOverlay: Color = Color.Unspecified,
) {

    Greenapple(
        GreenappleColor.Dark.colorScheme,
        GreenappleColor.Light.colorScheme,
    ),

    Midnightdusk(
        MidnightduskColor.Dark.colorScheme,
        MidnightduskColor.Light.colorScheme,
        MidnightduskColor.Dark.elevationOverlay,
        MidnightduskColor.Light.elevationOverlay,
    ),

    Strawberry(
        StrawberryColor.Dark.colorScheme,
        StrawberryColor.Light.colorScheme,
    ),

    Tachiyomi(
        TachiyomiColor.Dark.colorScheme,
        TachiyomiColor.Light.colorScheme,
    ),

    Tako(
        TakoColor.Dark.colorScheme,
        TakoColor.Light.colorScheme,
        TakoColor.Dark.elevationOverlay,
        TakoColor.Light.elevationOverlay
    ),

    Tealturqoise(
        TealturqoiseColor.Dark.colorScheme,
        TealturqoiseColor.Light.colorScheme,
        TealturqoiseColor.Dark.elevationOverlay,
        TealturqoiseColor.Light.elevationOverlay
    )

}
