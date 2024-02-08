package com.heyanle.easybangumi4.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.utils.loge
import com.heyanle.injekt.core.Injekt

/**
 * Created by HeYanLe on 2023/2/19 0:02.
 * https://github.com/heyanLE
 */

@Composable
fun NormalSystemBarColor(
    getStatusBarDark: (Boolean) -> Boolean = { !it },
    getNavigationBarDark: (Boolean) -> Boolean = { !it },
) {
    val themeController: EasyThemeController by Injekt.injectLazy()
    val themeState by themeController.themeFlow.collectAsState()
    val isDark = when (themeState.darkMode) {
        SettingPreferences.DarkMode.Dark -> true
        SettingPreferences.DarkMode.Light -> false
        else -> isSystemInDarkTheme()
    }

    val view = LocalView.current
    val activity = LocalContext.current as Activity
    if (!view.isInEditMode) {
        SideEffect {
            activity.window.navigationBarColor = Color.Transparent.toArgb()
            activity.window.statusBarColor = Color.Transparent.toArgb()
            WindowInsetsControllerCompat(
                activity.window,
                activity.window.decorView
            ).let { controller ->
                controller.isAppearanceLightStatusBars = !getStatusBarDark(isDark)
                controller.isAppearanceLightNavigationBars = !getNavigationBarDark(isDark)
            }
        }
    }
}

@Composable
fun EasyTheme(
    content: @Composable () -> Unit
) {
    val themeController: EasyThemeController by Injekt.injectLazy()
    val themeState by themeController.themeFlow.collectAsState()
    themeState.loge("EasyTheme")

    val isDynamic = themeState.isDynamicColor && themeController.isSupportDynamicColor()
    val isDark = when (themeState.darkMode) {
        SettingPreferences.DarkMode.Dark -> true
        SettingPreferences.DarkMode.Light -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        isDynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> {
            themeState.themeMode.getColorScheme(isDark)
        }
    }


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )

}