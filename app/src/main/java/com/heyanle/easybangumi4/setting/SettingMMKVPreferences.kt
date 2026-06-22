package com.heyanle.easybangumi4.setting

import com.heyanle.easybangumi4.base.preferences.mmkv.MMKVPreferenceStore

class SettingMMKVPreferences(
    private val preferenceStore: MMKVPreferenceStore,
) {

    val webViewCompatible = preferenceStore.getBoolean("web_view_compatible", false)

    val localExtensionTemp: Boolean by lazy {
        localExtensionPage.get()
    }
    val localExtensionPage = preferenceStore.getBoolean("local_extension_page", false)
}
