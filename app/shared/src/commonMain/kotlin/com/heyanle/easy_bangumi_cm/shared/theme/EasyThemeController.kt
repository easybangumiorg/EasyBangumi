package com.heyanle.easy_bangumi_cm.shared.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.heyanle.easy_bangumi_cm.base.service.system.IPlatformInformation
import com.heyanle.easy_bangumi_cm.base.utils.CoroutineProvider
import com.heyanle.easy_bangumi_cm.base.utils.preference.PreferenceStore
import com.heyanle.easy_bangumi_cm.base.utils.preference.getEnum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Created by heyanlin on 2025/1/26.
 */
class EasyThemeController(
    private val preferenceStore: PreferenceStore,
    private val platformInformation: IPlatformInformation
) {

    companion object {
        const val PREFERENCE_KEY_DARK_MODE = "dark_mode"
        const val PREFERENCE_KEY_IS_DYNAMIC = "dynamic_color"
        const val PREFERENCE_KEY_THEME_MODE = "theme_mode"
    }

    data class EasyThemeState(
        val themeMode: EasyThemeMode,
        val darkMode: DarkMode,
        val isDynamicColor: Boolean,
    ) {

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

    enum class DarkMode {
        Auto, Dark, Light
    }

    private val scope = CoroutineScope(SupervisorJob() + CoroutineProvider.main)

    private val darkModeStore = preferenceStore.getEnum(PREFERENCE_KEY_DARK_MODE, DarkMode.Auto)
    private val isDynamicColorStore = preferenceStore.getBoolean(PREFERENCE_KEY_IS_DYNAMIC, true)
    private val themeModeStore = preferenceStore.getEnum(PREFERENCE_KEY_THEME_MODE, EasyThemeMode.Default)

    val themeFlow = combine(
        darkModeStore.stateIn(scope),
        isDynamicColorStore.stateIn(scope),
        themeModeStore.stateIn(scope)
    ) { darkMode, isDynamic, themeMode ->
        combine(darkMode, isDynamic, themeMode)
    }.stateIn(
        scope,
        SharingStarted.Lazily,
        combine(darkModeStore.defaultValue(), isDynamicColorStore.defaultValue(), themeModeStore.defaultValue())
    )

    fun isSupportDynamicColor(): Boolean = platformInformation.isSupportDynamicColor()

    private fun combine(
        darkMode: DarkMode,
        isDynamicColor: Boolean,
        themeMode: EasyThemeMode
    ) = EasyThemeState(themeMode, darkMode, isDynamicColor)

}

expect fun IPlatformInformation.isSupportDynamicColor(): Boolean