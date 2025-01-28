package com.heyanle.easy_bangumi_cm.common.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.heyanle.easy_bangumi_cm.common.i18n.Res
import com.heyanle.easy_bangumi_cm.common.i18n.*
import com.heyanle.easy_bangumi_cm.common.theme.colors.*
import org.jetbrains.compose.resources.StringResource


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
    val titleResId: StringResource,
    val darkElevationOverlay: Color = Color.Unspecified,
    val lightElevationOverlay: Color = Color.Unspecified,
) {

    Default(
        DefaultColor.Dark.colorScheme,
        DefaultColor.Light.colorScheme,
        Res.string.theme_default
    ),

    Greenapple(
        GreenappleColor.Dark.colorScheme,
        GreenappleColor.Light.colorScheme,
        Res.string.theme_greenapple
    ),

    Midnightdusk(
        MidnightduskColor.Dark.colorScheme,
        MidnightduskColor.Light.colorScheme,
        Res.string.theme_midnightdusk,
        MidnightduskColor.Dark.elevationOverlay,
        MidnightduskColor.Light.elevationOverlay,
    ),

    Strawberry(
        StrawberryColor.Dark.colorScheme,
        StrawberryColor.Light.colorScheme,
        Res.string.theme_strawberry,
    ),



    Tako(
        TakoColor.Dark.colorScheme,
        TakoColor.Light.colorScheme,
        Res.string.theme_tako,
        TakoColor.Dark.elevationOverlay,
        TakoColor.Light.elevationOverlay
    ),

    Tealturqoise(
        TealturqoiseColor.Dark.colorScheme,
        TealturqoiseColor.Light.colorScheme,
        Res.string.theme_tealturqoise,
        TealturqoiseColor.Dark.elevationOverlay,
        TealturqoiseColor.Light.elevationOverlay
    )

}
