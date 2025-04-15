package org.easybangumi.next.shared.plugin.api.source


/**
 * Created by HeYanLe on 2024/12/8 21:00.
 * https://github.com/heyanLE
 */

interface Source {

    companion object {
        const val TYPE_PLAY = "play"
        const val TYPE_META = "meta"
    }

    val manifest: SourceManifest

    val key: String get() = manifest.key

}