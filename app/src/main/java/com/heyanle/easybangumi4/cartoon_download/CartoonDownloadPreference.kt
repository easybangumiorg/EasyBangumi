package com.heyanle.easybangumi4.cartoon_download

import com.heyanle.easybangumi4.base.preferences.android.AndroidPreferenceStore

/**
 * Created by heyanle on 2024/7/7.
 * https://github.com/heyanLE
 */
class CartoonDownloadPreference(
    private val androidPreferenceStore: AndroidPreferenceStore
) {

    val downloadMaxCountPref = androidPreferenceStore.getInt("download_max_count", 3)

    val downloadMaxCountPreSourcePref = androidPreferenceStore.getInt("download_max_count_pre_source", 1)
}