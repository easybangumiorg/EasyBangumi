package com.heyanle.easybangumi4.base.theme

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.heyanle.easybangumi4.setting.SettingPreferences
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/2/18 23:49.
 * https://github.com/heyanLE
 */

data class EasyThemeState(
    val themeMode: EasyThemeMode,
    val darkMode: SettingPreferences.DarkMode,
    val isDynamicColor: Boolean,
) {
    @SuppressLint("ComposableNaming")
    @Composable
    @ReadOnlyComposable
    fun isDark(): Boolean {
        return when (darkMode) {
            SettingPreferences.DarkMode.Dark -> true
            SettingPreferences.DarkMode.Light -> false
            else -> isSystemInDarkTheme()
        }
    }
}

class EasyThemeController(
    private val settingPreferences: SettingPreferences
) {

    private val scope = MainScope()

    private val _themeFlow = MutableStateFlow(
        EasyThemeState(
            settingPreferences.themeMode.get(),
            settingPreferences.darkMode.get(),
            settingPreferences.isThemeDynamic.get()
        )
    )
    val themeFlow = _themeFlow.asStateFlow()

    init {
        scope.launch {
            combine(
                settingPreferences.isThemeDynamic.flow(),
                settingPreferences.darkMode.flow(),
                settingPreferences.themeMode.flow(),
            ) { dy, dark, theme ->
                EasyThemeState(
                    theme, dark, dy
                )
            }.distinctUntilChanged().collectLatest {
                _themeFlow.update {
                    it
                }
            }
        }
    }


    fun changeDarkMode(darkMode: SettingPreferences.DarkMode) {
        settingPreferences.darkMode.set(darkMode)
    }

    fun changeThemeMode(
        themeMode: EasyThemeMode,
        isDynamicColor: Boolean = themeFlow.value.isDynamicColor
    ) {
        settingPreferences.isThemeDynamic.set(isDynamicColor)
        settingPreferences.themeMode.set(themeMode)
    }

    fun isSupportDynamicColor(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }


}