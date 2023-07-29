package com.heyanle.easybangumi4.source

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
    data class SourceConfig(
        val key: String,
        val enable: Boolean,
        val order: Int,
    )


    // 源配置
    val configs = preferenceStore.getObject(
        "source_config",
        mapOf<String, SourceConfig>(),
        {
            it.toJson()
        },
        {
            it.jsonTo()
        }
    )


}