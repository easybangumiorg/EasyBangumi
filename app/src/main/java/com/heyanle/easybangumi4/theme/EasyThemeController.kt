package com.heyanle.easybangumi4.theme

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.heyanle.easybangumi4.utils.loge
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.injekt.api.addSingletonFactory
import com.heyanle.injekt.api.get
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
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
                settingPreferences.isThemeDynamic.flow().stateIn(scope),
                settingPreferences.darkMode.flow().distinctUntilChanged().stateIn(scope),
                settingPreferences.themeMode.flow().distinctUntilChanged().stateIn(scope),
            ) { dy, dark, theme ->
                theme.loge("EasyTheme")
                EasyThemeState(
                    theme, dark, dy
                )
            }.collectLatest {new ->
                _themeFlow.update {
                    it.loge("EasyTheme")
                    new
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