package com.heyanle.easybangumi4.setting

import com.heyanle.easybangumi4.base.preferences.mmkv.MMKVPreferenceStore
import kotlin.text.get

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

    // 需要重启更新
    val extensionV2Temp: Boolean by lazy {
        extensionV2.get()
    }
    // 是否开启了扩展 v2
    val extensionV2 = preferenceStore.getBoolean("extension_v2", true)

    val localExtensionTemp: Boolean by lazy {
        localExtensionPage.get()
    }
    // 本地番源是否添加首页
    val localExtensionPage = preferenceStore.getBoolean("local_extension_page", false)

}