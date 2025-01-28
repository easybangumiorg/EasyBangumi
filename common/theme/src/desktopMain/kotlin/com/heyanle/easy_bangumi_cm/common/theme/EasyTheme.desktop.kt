package com.heyanle.easy_bangumi_cm.common.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * Desktop 没有 SystemBar
 * Created by HeYanLe on 2023/2/19 0:02.
 * https://github.com/heyanLE
 */
@Composable
actual fun NormalSystemBarColor(
    getStatusBarDark: (Boolean) -> Boolean,
    getNavigationBarDark: (Boolean) -> Boolean
) { }


actual fun hookColorScheme(
    isDynamic: Boolean,
    isDark: Boolean,
    themeState: EasyThemeController.EasyThemeState
): ColorScheme {
    return themeState.themeMode.getColorScheme(isDark)
}