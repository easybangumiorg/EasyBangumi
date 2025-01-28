package com.heyanle.easy_bangumi_cm.common.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.heyanle.lib.inject.core.Inject

/**
 * Created by HeYanLe on 2023/2/19 0:02.
 * https://github.com/heyanLE
 */

@Composable
expect fun NormalSystemBarColor(
    getStatusBarDark: (Boolean) -> Boolean = { !it },
    getNavigationBarDark: (Boolean) -> Boolean = { !it },
)

expect fun hookColorScheme(
    isDynamic: Boolean,
    isDark: Boolean,
    themeState: EasyThemeController.EasyThemeState
): ColorScheme

@Composable
fun EasyTheme(
    content: @Composable () -> Unit
) {
    val themeController: EasyThemeController by Inject.injectLazy()
    val themeState by themeController.themeFlow.collectAsState()

    val isDynamic = themeState.isDynamicColor && themeController.isSupportDynamicColor()
    val isDark = when (themeState.darkMode) {
        EasyThemeController.DarkMode.Dark -> true
        EasyThemeController.DarkMode.Light -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = hookColorScheme(isDynamic, isDark, themeState)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )

}