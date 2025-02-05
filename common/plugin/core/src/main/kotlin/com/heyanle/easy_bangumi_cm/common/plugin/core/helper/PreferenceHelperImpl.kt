package com.heyanle.easy_bangumi_cm.common.plugin.core.helper

import com.heyanle.easy_bangumi_cm.base.utils.HeKV
import com.heyanle.easy_bangumi_cm.plugin.entity.SourceManifest
import com.heyanle.easy_bangumi_cm.plugin.utils.PreferenceHelper
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString


/**
 * Created by HeYanLe on 2025/2/5 22:14.
 * https://github.com/heyanLE
 */

class PreferenceHelperImpl(
    private val sourceManifest: SourceManifest
): PreferenceHelper {

    private val hekv : HeKV by lazy {
        HeKV(
            Path(sourceManifest.extensionManifest.workPath, "preferences").absolutePathString(),
            sourceManifest.key
        )
    }

    override fun map(): Map<String, String> {
        return hekv.map()
    }

    override fun get(key: String, def: String): String {
        return hekv.get(key, def)
    }

    override fun put(key: String, value: String) {
        hekv.put(key, value)
    }
}