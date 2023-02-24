package com.heyanle.easybangumi.theme

import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.with
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay

/**
 * Created by HeYanLe on 2023/1/7 13:08.
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EasyTheme(
    content: @Composable () -> Unit
) {

    val easyThemeState by EasyThemeController.easyThemeState
    AnimatedContent(
        targetState = easyThemeState,
        transitionSpec = {
            fadeIn(animationSpec = tween(300, delayMillis = 0)) with
                    fadeOut(animationSpec = tween(300, delayMillis = 300))
        },
    ) {
        val isDynamic = it.isDynamicColor && EasyThemeController.isSupportDynamicColor()
        val isDark = when (it.darkMode) {
            DarkMode.Dark -> true
            DarkMode.Light -> false
            else -> isSystemInDarkTheme()
        }

        val colorScheme = when {
            isDynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

            }

            else -> {
                Log.d("EasyTheme", it.themeMode.name)
                val old = it.themeMode.getColorScheme(isDark)
                // 不想搞 md3 配色
                old.copy(
                    primaryContainer = old.background,
                    onPrimaryContainer = old.onBackground,
                    secondaryContainer = old.surface,
                    onSecondaryContainer = old.onSurface,
                    tertiary = old.secondary,
                    tertiaryContainer = old.secondary,
                    onTertiary = old.onSecondary,
                    onTertiaryContainer = old.onSecondary,
                    surfaceVariant = old.surface,
                    onSurfaceVariant = old.onSurface
                )
            }
        }

        val uiController = rememberSystemUiController()
        val view = LocalView.current
        if (!view.isInEditMode) {
            SideEffect {
                uiController.setStatusBarColor(Color.Transparent, if (isDynamic) isDark else false)
                uiController.setNavigationBarColor(
                    colorScheme.primary,
                    if (isDynamic) isDark else false
                )
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


}