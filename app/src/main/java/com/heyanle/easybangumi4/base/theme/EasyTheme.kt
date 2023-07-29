package com.heyanle.easybangumi4.base.theme

import android.os.Build
import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.google.accompanist.systemuicontroller.rememberSystemUiController

/**
 * Created by HeYanLe on 2023/2/19 0:02.
 * https://github.com/heyanLE
 */

@Composable
fun NormalSystemBarColor(
    getStatusBarDark: (Boolean)->Boolean = {!it},
    getNavigationBarDark: (Boolean)->Boolean = {!it},
){
    val isDark = when (EasyThemeController.easyThemeState.value.darkMode) {
        DarkMode.Dark -> true
        DarkMode.Light -> false
        else -> isSystemInDarkTheme()
    }

    val uiController = rememberSystemUiController()
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect() {
            uiController.setStatusBarColor(Color.Transparent, getStatusBarDark(isDark))
            uiController.setNavigationBarColor(Color.Transparent, getNavigationBarDark(isDark))
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EasyTheme(
    content: @Composable () -> Unit
) {

    val easyThemeState by EasyThemeController.easyThemeState

    val isDynamic = easyThemeState.isDynamicColor && EasyThemeController.isSupportDynamicColor()
    val isDark = when (easyThemeState.darkMode) {
        DarkMode.Dark -> true
        DarkMode.Light -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        isDynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark ) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

        }

        else -> {
            Log.d("EasyTheme", easyThemeState.themeMode.name)
            easyThemeState.themeMode.getColorScheme(isDark)
        }
    }

    LaunchedEffect(key1 = colorScheme) {
        EasyThemeController.curThemeColor = colorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )


}