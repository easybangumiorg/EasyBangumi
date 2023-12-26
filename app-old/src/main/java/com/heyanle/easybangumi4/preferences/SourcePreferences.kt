package com.heyanle.easybangumi4.preferences

import com.heyanle.bangumi_source_api.api.Source
import com.heyanle.easybangumi4.base.preferences.Preference
import com.heyanle.easybangumi4.base.preferences.PreferenceStore
import com.heyanle.easybangumi4.utils.jsonTo
import com.heyanle.easybangumi4.utils.toJson

/**
 * Created by HeYanLe on 2023/7/29 21:34.
 * https://github.com/heyanLE
 */
class SourcePreferences(
    private val preferenceStore: PreferenceStore
) {
    data class LocalSourceConfig(
        val key: String,
        val enable: Boolean,
        val order: Int,
        val versionCode: Int = 0,
    )


    // 源配置
    val configs = preferenceStore.getObject(
        "source_config",
        mapOf<String, LocalSourceConfig>(),
        {
            it.toJson()
        },
        {
            it.jsonTo()?: mapOf()
        }
    )

    // 获取本地存储的源版本，用于判断是否需要迁移
    fun getLastVersion(
        source: Source
    ): Preference<Int>{
        return preferenceStore.getInt("version-${source.key}", source.versionCode)
    }


}