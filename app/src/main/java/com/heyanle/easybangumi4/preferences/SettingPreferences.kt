package com.heyanle.easybangumi4.preferences

import android.app.Application
import android.os.Environment
import com.heyanle.easybangumi4.base.preferences.PreferenceStore
import com.heyanle.easybangumi4.base.preferences.getEnum
import com.heyanle.easybangumi4.base.theme.EasyThemeMode
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

    // 是否在收藏里显示更新
    val isShowUpdateInStar = preferenceStore.getBoolean("update_in_star", true)

    // 播放设置
    enum class PlayerOrientationMode {
        Auto, Enable, Disable
    }
    val playerOrientationMode = preferenceStore.getEnum<PlayerOrientationMode>("player_orientation_mode", PlayerOrientationMode.Auto)

    // 使用外置播放器
    var useExternalVideoPlayer = preferenceStore.getBoolean("use_external_video_player", false)

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
            add(File(it, "easyBangumi").absolutePath to stringRes(com.heyanle.easy_i18n.R.string.public_download_path))
        }
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)?.let {
            add(File(it, "easyBangumi").absolutePath to stringRes(com.heyanle.easy_i18n.R.string.public_movie_path))
        }
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)?.let {
            add(File(it, "easyBangumi").absolutePath to stringRes(com.heyanle.easy_i18n.R.string.public_dcim_path))
        }
    }
    var downloadPath = preferenceStore.getString("cartoon_download_path", application.getFilePath("download") )



}