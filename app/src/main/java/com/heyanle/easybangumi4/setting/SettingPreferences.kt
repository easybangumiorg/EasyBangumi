package com.heyanle.easybangumi4.setting

import android.app.Application
import com.heyanle.easybangumi4.base.preferences.PreferenceStore
import com.heyanle.easybangumi4.base.preferences.getEnum
import com.heyanle.easybangumi4.theme.EasyThemeMode
import com.heyanle.easybangumi4.utils.mb
import com.heyanle.easybangumi4.utils.stringRes
import loli.ball.easyplayer2.utils.MeasureHelper

/**
 * 设置
 * Created by HeYanLe on 2023/7/29 17:39.
 * https://github.com/heyanLE
 */
class SettingPreferences(
    private val application: Application,
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

    // 播放设置
    enum class PlayerOrientationMode {
        Auto, Enable, Disable
    }

    val playerOrientationMode = preferenceStore.getEnum<PlayerOrientationMode>(
        "player_orientation_mode",
        PlayerOrientationMode.Auto
    )

    // 使用外置播放器
    var useExternalVideoPlayer = preferenceStore.getBoolean("use_external_video_player", false)

    var playerBottomNavigationBarPadding =
        preferenceStore.getBoolean("player_bottom_nav_padding", true)

    val cacheSizeSelection = listOf(
        0L to stringRes(com.heyanle.easy_i18n.R.string.disable),
        500L.mb to "500MB",
        1000L.mb to "1GB",
        2000L.mb to "2GB",
        3000L.mb to "3GB",
        5000L.mb to "5GB",
    )

    // 最大缓存
    var cacheSize = preferenceStore.getLong("cache_size", 2000.mb.toLong())

    val customSpeed = preferenceStore.getFloat("custom_speed", -1f)

    val fastWeightSelection = listOf(2, 3, 4, 5, 6)
    // 双击快进区域占屏幕宽度的比例的倒数。只能是 2, 3, 4, 5, 6 与其相反数。负数代表关闭
    val fastWeight = preferenceStore.getInt("fast_space_weight", 5)

    val fastWeightTopDenominator = 6
    val fastWeightTopMoleculeSelection = listOf(1, 2, 3, 4, 5, )
    // 双击快进顶部区域占屏幕高度的比例分子，分母是 6，这里只能是 1 2 3 4 5。负数代表关闭
    val fastWeightTopMolecule = preferenceStore.getInt("fast_space_weight_top", -3)

    // 双击快进快退的时间（秒）
    val fastSecond = preferenceStore.getInt("fast_second", 15)
    // 顶部双击快进快退的时间（秒）
    val fastTopSecond = preferenceStore.getInt("fast_second_top", 30)


    // 详情页集列数
    val detailedScreenEpisodeGridCount = preferenceStore.getInt("detailed_screen_episode_grid_count", 2)


    val scaleTypeSelection = listOf(
        MeasureHelper.SCREEN_SCALE_DEFAULT to com.heyanle.easy_i18n.R.string.video_scale_type_default,
        MeasureHelper.SCREEN_SCALE_16_9 to com.heyanle.easy_i18n.R.string.video_scale_type_16_9,
        MeasureHelper.SCREEN_SCALE_4_3 to com.heyanle.easy_i18n.R.string.video_scale_type_4_3,
        MeasureHelper.SCREEN_SCALE_MATCH_PARENT to com.heyanle.easy_i18n.R.string.video_scale_type_match_parent,
        MeasureHelper.SCREEN_SCALE_ORIGINAL to com.heyanle.easy_i18n.R.string.video_scale_type_original,
        MeasureHelper.SCREEN_SCALE_CENTER_CROP to com.heyanle.easy_i18n.R.string.video_scale_type_center_crop,
        MeasureHelper.SCREEN_SCALE_ADAPT to com.heyanle.easy_i18n.R.string.video_scale_type_adapt,)
    // 视频填充模式
    val videoScaleType = preferenceStore.getInt("video_scale_type", 0)

    // 默认倍速（-1 表示默认为当前自定义）
    val defaultSpeed = preferenceStore.getFloat("default_speed", 1f)

    // 播放器拖动屏幕拉进度条的幅度（当前视频总宽度占的视频时间）
    val playerSeekFullWidthTimeMS = preferenceStore.getLong("player_seek_full_width_time_ms", 300000)


    // ======================================
    // 是否使用私有目录
    val localUsePrivate = preferenceStore.getBoolean("local_use_private", true)


    // 选择文件夹 uri 和路径（仅供展示）
    val localUri = preferenceStore.getString("local_folder_uri", "")
    val localPath = preferenceStore.getString("local_folder_path", "")


    // 启动后自动同步仓库中的所有源（下载 or 更新）
    val sourceAutoSync = preferenceStore.getBoolean("launch_auto_sync", true)



}