package com.heyanle.easybangumi4.setting

import com.heyanle.easybangumi4.base.preferences.mmkv.MMKVPreferenceStore

/**
 * 一些配置需要很早时候，因此不能存 sp，需要存 mmkv
 * Created by HeYanLe on 2023/7/30 19:31.
 * https://github.com/heyanLE
 */
class SettingMMKVPreferences(
    private val preferenceStore: MMKVPreferenceStore
) {

    // WebView 兼容模式
    val webViewCompatible = preferenceStore.getBoolean("web_view_compatible", false)

}