package com.heyanle.easybangumi4.v2.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.heyanle.easybangumi4.setting.SettingPreferences
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class V2ThemeColorSet(
    val accent: Color,
    val onAccent: Color,
    val accentContainer: Color,
    val onAccentContainer: Color,
)

/** Accent palettes available only to the V2 presentation layer. */
enum class V2ThemeColor(
    val storageKey: String,
    val displayName: String,
    val day: V2ThemeColorSet,
    val night: V2ThemeColorSet,
) {
    BrandYellow(
        storageKey = "brand_yellow",
        displayName = "品牌黄",
        day = V2ThemeColorSet(
            accent = Color(0xFF9A6900),
            onAccent = Color(0xFFFFFFFF),
            accentContainer = Color(0xFFFFF1BF),
            onAccentContainer = Color(0xFF3D2F00),
        ),
        night = V2ThemeColorSet(
            accent = Color(0xFFFFD45A),
            onAccent = Color(0xFF382B00),
            accentContainer = Color(0xFF493900),
            onAccentContainer = Color(0xFFFFE58B),
        ),
    ),
    Persimmon(
        storageKey = "persimmon",
        displayName = "柿子橙",
        day = V2ThemeColorSet(
            accent = Color(0xFFB9481A),
            onAccent = Color(0xFFFFFFFF),
            accentContainer = Color(0xFFFFE1D2),
            onAccentContainer = Color(0xFF4D1907),
        ),
        night = V2ThemeColorSet(
            accent = Color(0xFFFF9C74),
            onAccent = Color(0xFF4B1705),
            accentContainer = Color(0xFF56200E),
            onAccentContainer = Color(0xFFFFDBCC),
        ),
    ),
    Jade(
        storageKey = "jade",
        displayName = "青玉绿",
        day = V2ThemeColorSet(
            accent = Color(0xFF147A63),
            onAccent = Color(0xFFFFFFFF),
            accentContainer = Color(0xFFD2F4E8),
            onAccentContainer = Color(0xFF063B2F),
        ),
        night = V2ThemeColorSet(
            accent = Color(0xFF5AD6B5),
            onAccent = Color(0xFF00382C),
            accentContainer = Color(0xFF0C4437),
            onAccentContainer = Color(0xFF8DF1D2),
        ),
    ),
    Ocean(
        storageKey = "ocean",
        displayName = "海湾蓝",
        day = V2ThemeColorSet(
            accent = Color(0xFF2769B2),
            onAccent = Color(0xFFFFFFFF),
            accentContainer = Color(0xFFD9E9FF),
            onAccentContainer = Color(0xFF0E365F),
        ),
        night = V2ThemeColorSet(
            accent = Color(0xFF78B7FF),
            onAccent = Color(0xFF002E52),
            accentContainer = Color(0xFF153C63),
            onAccentContainer = Color(0xFFA8CEFF),
        ),
    ),
    Wisteria(
        storageKey = "wisteria",
        displayName = "藤萝紫",
        day = V2ThemeColorSet(
            accent = Color(0xFF7655AD),
            onAccent = Color(0xFFFFFFFF),
            accentContainer = Color(0xFFECE1FF),
            onAccentContainer = Color(0xFF352057),
        ),
        night = V2ThemeColorSet(
            accent = Color(0xFFC5A3FF),
            onAccent = Color(0xFF321653),
            accentContainer = Color(0xFF402B5D),
            onAccentContainer = Color(0xFFE2C8FF),
        ),
    );

    fun colors(isDark: Boolean): V2ThemeColorSet = if (isDark) night else day

    companion object {
        fun fromStorage(storageKey: String): V2ThemeColor =
            entries.firstOrNull { it.storageKey == storageKey } ?: BrandYellow
    }
}

@Immutable
data class V2ThemeState(
    val themeColor: V2ThemeColor,
)

/**
 * Owns V2 theme state and persistence, mirroring [com.heyanle.easybangumi4.theme.EasyThemeController]
 * without coupling the new visual system to the legacy activity.
 */
class V2ThemeController(
    private val settingPreferences: SettingPreferences,
) {
    private val scope = MainScope()
    private val _themeFlow = MutableStateFlow(
        V2ThemeState(V2ThemeColor.fromStorage(settingPreferences.v2ThemeColor.get())),
    )
    val themeFlow = _themeFlow.asStateFlow()

    init {
        scope.launch {
            settingPreferences.v2ThemeColor.flow()
                .distinctUntilChanged()
                .collectLatest { storedColor ->
                    _themeFlow.update {
                        V2ThemeState(V2ThemeColor.fromStorage(storedColor))
                    }
                }
        }
    }

    fun changeThemeColor(themeColor: V2ThemeColor) {
        settingPreferences.v2ThemeColor.set(themeColor.storageKey)
    }
}
