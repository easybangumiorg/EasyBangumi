package com.heyanle.easy_bangumi_cm.plugin.api.source

import com.heyanle.easy_bangumi_cm.plugin.api.component.ComponentBundle
import kotlin.reflect.KClass


/**
 * Created by HeYanLe on 2024/12/8 21:00.
 * https://github.com/heyanLE
 */

interface Source {

    companion object {
        const val TYPE_MEDIA = "media"
        const val TYPE_META = "meta"
    }

    val manifest: SourceManifest
    val sourceIndex
        get() = "${manifest.type}-${manifest.key}"

}