package com.heyanle.easybangumi4.setting

import android.app.Application
import android.os.Environment
import com.heyanle.easybangumi4.base.preferences.PreferenceStore
import com.heyanle.easybangumi4.base.preferences.getEnum
import com.heyanle.easybangumi4.theme.EasyThemeMode
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.easybangumi4.utils.mb
import com.heyanle.easybangumi4.utils.stringRes
import java.io.File

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

    // 缓存番剧数据过期时间（小时）
    var cartoonInfoCacheTimeHour = preferenceStore.getLong("cartoon_cache_time_hour", 12)

    // 下载番剧路径
    val downloadPathSelection = arrayListOf<Pair<String, String>>().apply {
        add(application.getFilePath("download") to stringRes(com.heyanle.easy_i18n.R.string.private_download_path))
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)?.let {
            add(File(it, "EasyBangumi").absolutePath to stringRes(com.heyanle.easy_i18n.R.string.public_download_path))
        }
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)?.let {
            add(File(it, "EasyBangumi").absolutePath to stringRes(com.heyanle.easy_i18n.R.string.public_movie_path))
        }
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)?.let {
            add(File(it, "EasyBangumi").absolutePath to stringRes(com.heyanle.easy_i18n.R.string.public_dcim_path))
        }
    }
    // 需要刷新媒体的路径
    val needRefreshMedia = hashSetOf<String>().apply {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)?.let {
            add(File(it, "EasyBangumi").absolutePath)
        }
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)?.let {
            add(File(it, "EasyBangumi").absolutePath)
        }
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)?.let {
            add(File(it, "EasyBangumi").absolutePath)
        }
    }
    val downloadPath = preferenceStore.getString("cartoon_download_path", application.getFilePath("download") )

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

}