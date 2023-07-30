package com.heyanle.easybangumi4.preferences

import com.heyanle.easybangumi4.base.preferences.PreferenceStore
import com.heyanle.easybangumi4.utils.jsonTo
import com.heyanle.easybangumi4.utils.toJson

/**
 * Created by HeYanLe on 2023/7/30 13:10.
 * https://github.com/heyanLE
 */
class CartoonPreferences(
    private val preferenceStore: PreferenceStore
) {

    data class CartoonTag(
        val label: String,
        val order: Int,
    )
    // 番剧标签
    val tags = preferenceStore.getObject(
        "cartoon_tags",
        listOf<CartoonTag>(),
        {
            it.toJson()
        },
        {
            it.jsonTo()
        }
    )
}