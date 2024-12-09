package com.heyanle.easy_bangumi_cm.plugin.core.source.inner.debug.media

import com.heyanle.easy_bangumi_cm.plugin.api.source.MediaSource
import com.heyanle.easy_bangumi_cm.plugin.api.source.SourceManifest
import kotlin.reflect.KClass

/**
 * Created by heyanlin on 2024/12/9.
 */
class DebugMediaSource: MediaSource {

    override val manifest: SourceManifest by lazy {
        SourceManifest(
            key = "debug_media",
            type = "media",
            label = "调试番源",
            version = 1,
        )
    }
    override val source: String
        get() = manifest.key

    override fun register(): List<KClass<*>> {
        return listOf(
            DebugHomeComponent::class
        )
    }
}