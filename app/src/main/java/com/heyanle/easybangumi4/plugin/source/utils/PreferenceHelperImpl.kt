package com.heyanle.easybangumi4.plugin.source.utils

import com.heyanle.easybangumi4.base.hekv.HeKV
import com.heyanle.easybangumi4.source_api.utils.api.PreferenceHelper

/**
 * Created by HeYanLe on 2023/10/29 16:31.
 * https://github.com/heyanLE
 */
class PreferenceHelperImpl(
    private val heKV: HeKV
): PreferenceHelper {

    override fun map(): Map<String, String> {
        return heKV.map()
    }
    override fun get(key: String, def: String): String {
        return heKV.get(key, def)
    }

    override fun put(key: String, value: String) {
        heKV.put(key, value)
    }


}