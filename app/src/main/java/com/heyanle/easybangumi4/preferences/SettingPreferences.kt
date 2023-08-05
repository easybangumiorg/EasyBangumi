package com.heyanle.easybangumi4.preferences

import com.heyanle.easybangumi4.base.preferences.PreferenceStore
import com.heyanle.easybangumi4.base.preferences.getEnum
import com.heyanle.easybangumi4.base.theme.EasyThemeMode

/**
 * 设置
 * Created by HeYanLe on 2023/7/29 17:39.
 * https://github.com/heyanLE
 */
class SettingPreferences(
    private val preferenceStore: PreferenceStore
) {

    // 无痕模式
    val isInPrivate = preferenceStore.getBoolean("in_private", false)

    // 外观设置
    // 夜间模式
    enum class DarkMode {
        Auto, Dark, Light
    }
    val darkMode = preferenceStore.getEnum<DarkMode>("dark_mode", DarkMode.Auto)

    // 主题设置
    val isThemeDynamic = preferenceStore.getBoolean("theme_dynamic", true)
    val themeMode = preferenceStore.getEnum<EasyThemeMode>("theme_mode", EasyThemeMode.Default)

    // 平板模式
    enum class PadMode {
        AUTO, ENABLE, DISABLE
    }
    val padMode = preferenceStore.getEnum<PadMode>("pad_mode", PadMode.AUTO)

    // 是否在收藏里显示更新
    val isShowUpdateInStar = preferenceStore.getBoolean("update_in_star", true)

    // 播放设置
    enum class PlayerOrientationMode {
        Auto, Enable, Disable
    }
    val playerOrientationMode = preferenceStore.getEnum<PlayerOrientationMode>("player_orientation_mode", PlayerOrientationMode.Auto)

    var useExternalVideoPlayer = preferenceStore.getBoolean("use_external_video_player", false)


}