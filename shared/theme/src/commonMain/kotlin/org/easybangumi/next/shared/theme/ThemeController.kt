package org.easybangumi.next.shared.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.easybangumi.next.lib.store.preference.PreferenceStore
import org.easybangumi.next.lib.store.preference.getEnum
import org.easybangumi.next.lib.utils.CoroutineProvider
import org.easybangumi.next.lib.utils.main
import org.koin.core.component.KoinComponent

/**
 * Created by heyanlin on 2025/4/9.
 */
class ThemeController(
    private val preferenceStore: PreferenceStore,
): KoinComponent {

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

    private val scope = CoroutineScope(SupervisorJob() + CoroutineProvider.main())

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

    private fun combine(
        darkMode: DarkMode,
        isDynamicColor: Boolean,
        themeMode: EasyThemeMode
    ) = EasyThemeState(themeMode, darkMode, isDynamicColor)


}