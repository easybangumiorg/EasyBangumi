package com.heyanle.easy_bangumi_cm.common.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import com.heyanle.lib.inject.core.Inject

/**
 * 各个页面配置 SystemBar 颜色
 * Created by HeYanLe on 2023/2/19 0:02.
 * https://github.com/heyanLE
 */
@Composable
actual fun NormalSystemBarColor(
    getStatusBarDark: (Boolean) -> Boolean,
    getNavigationBarDark: (Boolean) -> Boolean
) {

    val themeController: EasyThemeController by Inject.injectLazy()
    val themeState by themeController.themeFlow.collectAsState()
    val isDark = when (themeState.darkMode) {
        EasyThemeController.DarkMode.Dark -> true
        EasyThemeController.DarkMode.Light -> false
        else -> isSystemInDarkTheme()
    }

    val view = LocalView.current
    val activity = LocalContext.current as Activity
    if (!view.isInEditMode) {
        SideEffect {
            WindowInsetsControllerCompat(
                activity.window,
                activity.window.decorView
            ).let { controller ->
                controller.isAppearanceLightStatusBars = getStatusBarDark(isDark)
                controller.isAppearanceLightNavigationBars = getNavigationBarDark(isDark)
            }
        }
    }

}


actual fun hookColorScheme(
    isDynamic: Boolean,
    isDark: Boolean,
    themeState: EasyThemeController.EasyThemeState
): ColorScheme {
    val themeController: EasyThemeController by Inject.injectLazy()
    return if (isDynamic && themeController.isSupportDynamicColor() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        themeState.themeMode.getColorScheme(isDark)
    }
}