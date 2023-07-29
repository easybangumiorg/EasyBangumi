package com.heyanle.easybangumi4.base.theme

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.mutableStateOf
import com.heyanle.okkv2.core.okkv

/**
 * Created by HeYanLe on 2023/2/18 23:49.
 * https://github.com/heyanLE
 */
enum class DarkMode {
    Auto, Dark, Light
}

data class EasyThemeState(
    val themeMode: EasyThemeMode,
    val darkMode: DarkMode,
    val isDynamicColor: Boolean,
) {
    @SuppressLint("ComposableNaming")
    @Composable
    @ReadOnlyComposable
    fun isDark(): Boolean {
        return when (darkMode) {
            DarkMode.Dark -> true
            DarkMode.Light -> false
            else -> isSystemInDarkTheme()
        }
    }
}

object EasyThemeController {

    var curThemeColor: ColorScheme? = null

    private var themeModeOkkv by okkv("theme_mode", EasyThemeMode.Default.name)
    private var darkModeOkkv by okkv("dark_mode", DarkMode.Auto.name)
    private var isDynamicColorOkkv by okkv<Boolean>("is_dynamic_color", def = true)

    val easyThemeState = mutableStateOf(
        EasyThemeState(
            kotlin.runCatching { EasyThemeMode.valueOf(themeModeOkkv) }.getOrElse { EasyThemeMode.Default },
            DarkMode.valueOf(darkModeOkkv),
            isDynamicColorOkkv && isSupportDynamicColor()
        )
    )


    fun changeDarkMode(darkMode: DarkMode) {
        darkModeOkkv = darkMode.name
        easyThemeState.value = easyThemeState.value.copy(
            darkMode = darkMode
        )
    }

    fun changeThemeMode(themeMode: EasyThemeMode, isDynamicColor: Boolean = easyThemeState.value.isDynamicColor) {
        themeModeOkkv = themeMode.name
        isDynamicColorOkkv = isDynamicColor
        easyThemeState.value = easyThemeState.value.copy(
            themeMode = themeMode,
            isDynamicColor = isDynamicColor
        )

    }

    fun changeIsDynamicColor(isDynamicColor: Boolean) {
        // 安卓 12 才有该功能
        val real = (isDynamicColor && isSupportDynamicColor())
        isDynamicColorOkkv = real
        easyThemeState.value = easyThemeState.value.copy(
            isDynamicColor = isDynamicColor && isSupportDynamicColor()
        )
    }

    fun isSupportDynamicColor(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }


}