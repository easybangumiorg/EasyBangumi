package com.heyanle.easybangumi4.cartoon.story.download

import android.os.Build
import com.heyanle.easybangumi4.base.preferences.android.AndroidPreferenceStore
import com.heyanle.easybangumi4.base.preferences.getEnum

/**
 * Created by heyanle on 2024/7/7.
 * https://github.com/heyanLE
 */
class CartoonDownloadPreference(
    private val androidPreferenceStore: AndroidPreferenceStore
) {

    // 最大下载数量
    val downloadMaxCountPref = androidPreferenceStore.getLong("download_max_count", 3L)

    // 最大编解码数量
    val transformMaxCountPref = androidPreferenceStore.getLong("transform_max_count", 1L)

    val downloadMaxCountPreSourcePref = androidPreferenceStore.getInt("download_max_count_pre_source", 1)


    // 下载编码方式
    enum class DownloadEncode {
        H264, H265
    }
    val downloadEncodeSelection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        listOf(
            DownloadEncode.H264 to "H.264",
            DownloadEncode.H265 to "H.265",
        )
    } else {
        listOf(
            DownloadEncode.H264 to "H.264",
        )
    }

    val downloadEncode = androidPreferenceStore.getEnum<DownloadEncode>("download_encode",  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        DownloadEncode.H265
    } else {
        DownloadEncode.H264
    })

    // 是否让手机相册忽略本地文件夹
    var localNoMedia = androidPreferenceStore.getBoolean("local_path_no_media", true)


}