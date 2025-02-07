package com.heyanle.easy_bangumi_cm.plugin.api.source


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

    val key: String get() = manifest.key

}