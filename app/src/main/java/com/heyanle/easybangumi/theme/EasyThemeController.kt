package com.heyanle.easybangumi.theme

import android.os.Build
import androidx.compose.runtime.mutableStateOf
import com.heyanle.okkv2.core.okkv

/**
 * Created by HeYanLe on 2023/1/7 12:42.
 * https://github.com/heyanLE
 */

enum class DarkMode {
    Auto, Dark, Light
}

data class EasyThemeState(
    val themeMode: EasyThemeMode,
    val darkMode: DarkMode,
    val isDynamicColor: Boolean,
)


object EasyThemeController{

    private var themeModeOkkv by okkv("theme_mode_index", EasyThemeMode.Blue.name)
    private var darkModeOkkv by okkv("dark_mode_index", DarkMode.Auto.name)
    private var isDynamicColorOkkv by okkv<Boolean>("is_dynamic_color", def = false)

    val easyThemeState = mutableStateOf(
        EasyThemeState(
        EasyThemeMode.valueOf(themeModeOkkv),
        DarkMode.valueOf(darkModeOkkv),
        isDynamicColorOkkv && isSupportDynamicColor()
    )
    )



    fun changeDarkMode(darkMode: DarkMode){
        darkModeOkkv = darkMode.name
        easyThemeState.value = easyThemeState.value.copy(
            darkMode = darkMode
        )
    }

    fun changeThemeMode(themeMode: EasyThemeMode){
        themeModeOkkv = themeMode.name
        easyThemeState.value = easyThemeState.value.copy(
            themeMode = themeMode
        )

    }

    fun changeIsDynamicColor(isDynamicColor: Boolean){
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